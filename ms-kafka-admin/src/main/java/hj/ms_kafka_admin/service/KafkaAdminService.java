package hj.ms_kafka_admin.service;

import hj.ms_kafka_admin.config.KafkaAdminProperties;
import hj.ms_kafka_admin.dto.ClusterResponse;
import hj.ms_kafka_admin.dto.ParticionInfo;
import hj.ms_kafka_admin.dto.TopicoRequest;
import hj.ms_kafka_admin.dto.TopicoResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidConfigurationException;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KafkaAdminService {

    private static final int TIMEOUT_SEG = 15;
    private static final String CLEANUP_POLICY = "cleanup.policy";
    private static final String RETENTION_MS = "retention.ms";

    private final Admin admin;
    private final KafkaAdminProperties propiedades;

    public KafkaAdminService(Admin admin, KafkaAdminProperties propiedades) {
        this.admin = admin;
        this.propiedades = propiedades;
    }

    public ClusterResponse cluster() {
        var datos = admin.describeCluster();
        Collection<Node> nodos = esperar(datos.nodes());
        List<String> brokers = nodos.stream().map(n -> n.id() + " - " + n.host() + ":" + n.port()).toList();
        return new ClusterResponse(esperar(datos.clusterId()), esperar(datos.controller()).id(), brokers);
    }

    public List<TopicoResponse> listar() {
        Set<String> nombres = esperar(admin.listTopics().names());
        if (nombres.isEmpty()) return List.of();
        Map<String, TopicDescription> descripciones = esperar(admin.describeTopics(nombres).allTopicNames());
        return new TreeMap<>(descripciones).values().stream().map(this::aRespuesta).toList();
    }

    public TopicoResponse obtener(String nombre) {
        Map<String, TopicDescription> d = esperar(admin.describeTopics(List.of(nombre)).allTopicNames());
        return aRespuesta(d.get(nombre));
    }

    public void crear(TopicoRequest r) {
        NewTopic nuevo = new NewTopic(r.nombre(), r.particiones(), (short) r.replicas());
        if (r.configuraciones() != null) nuevo.configs(r.configuraciones());
        esperar(admin.createTopics(List.of(nuevo)).all());
    }

    /** Crea solo los que faltan (los definidos en application.yaml). Devuelve los nombres creados. */
    public List<String> crearTopicosBase() {
        Set<String> existentes = esperar(admin.listTopics().names());
        List<String> creados = new ArrayList<>();
        for (KafkaAdminProperties.TopicoBase t : propiedades.topicosBase()) {
            if (!existentes.contains(t.nombre())) {
                crear(aRequest(t));
                creados.add(t.nombre());
            }
        }
        return creados;
    }

    public void eliminar(String nombre) {
        esperar(admin.deleteTopics(List.of(nombre)).all());
    }

    public void cambiarConfiguracion(String nombre, Map<String, String> cambios) {
        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Collection<AlterConfigOp> ops = cambios.entrySet().stream()
            .map(e -> new AlterConfigOp(new ConfigEntry(e.getKey(), e.getValue()), AlterConfigOp.OpType.SET))
            .toList();
        esperar(admin.incrementalAlterConfigs(Map.of(recurso, ops)).all());
    }

    public void aumentarParticiones(String nombre, int total) {
        esperar(admin.createPartitions(Map.of(nombre, NewPartitions.increaseTo(total))).all());
    }

    /** Pasa un tópico del yaml (retención en días) a lo que entiende Kafka (retention.ms). */
    private TopicoRequest aRequest(KafkaAdminProperties.TopicoBase t) {
        String retencionMs = String.valueOf(Duration.ofDays(t.retencionDias()).toMillis());
        return new TopicoRequest(t.nombre(), t.particiones(), t.replicas(),
            Map.of(CLEANUP_POLICY, t.politica(), RETENTION_MS, retencionMs));
    }

    private TopicoResponse aRespuesta(TopicDescription d) {
        List<ParticionInfo> detalle = d.partitions().stream().map(this::aParticion).toList();
        int replicas = detalle.isEmpty() ? 0 : detalle.get(0).replicas().size();
        return new TopicoResponse(d.name(), detalle.size(), replicas, configuracionesPropias(d.name()), detalle);
    }

    private ParticionInfo aParticion(TopicPartitionInfo p) {
        return new ParticionInfo(
            p.partition(),
            p.leader() != null ? p.leader().id() : -1,
            p.replicas().stream().map(Node::id).toList(),
            p.isr().stream().map(Node::id).toList());
    }

    /** Solo las configuraciones puestas explícitamente en el tópico (no las del broker). */
    private Map<String, String> configuracionesPropias(String nombre) {
        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, nombre);
        Config config = esperar(admin.describeConfigs(List.of(recurso)).all()).get(recurso);
        Map<String, String> propias = new TreeMap<>();
        if (config != null) {
            config.entries().stream()
                .filter(e -> e.source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG)
                .forEach(e -> propias.put(e.name(), e.value()));
        }
        return propias;
    }

    private <T> T esperar(KafkaFuture<T> futuro) {
        try {
            return futuro.get(TIMEOUT_SEG, TimeUnit.SECONDS);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Operación interrumpida.");
        } catch (TimeoutException _) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Kafka no respondió a tiempo.");
        } catch (ExecutionException e) {
            throw traducir(e.getCause());
        }
    }

    private ResponseStatusException traducir(Throwable causa) {
        if (causa instanceof TopicExistsException) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "El tópico ya existe.");
        }
        if (causa instanceof UnknownTopicOrPartitionException) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "El tópico no existe.");
        }
        if (causa instanceof InvalidTopicException || causa instanceof InvalidReplicationFactorException
                || causa instanceof InvalidPartitionsException || causa instanceof InvalidConfigurationException) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, causa.getMessage());
        }
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error de Kafka: " + causa.getMessage());
    }
}

