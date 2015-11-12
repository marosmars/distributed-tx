package org.opendaylight.distributed.tx.spiimpl;

import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.distributed.tx.spi.TxProvider;
import org.opendaylight.distributed.tx.spi.TxException;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeFields.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;

/**
 * Per node transaction provider SPI. Distributed tx treats every node just as an instance of ReadWriteTransaction.
 * This provider interface hides the details of its creation, whether the per node transactions come from MountPoints or are app specific.
 */
public class TxProviderMountPointImpl implements TxProvider {

    private MountPointService mountService;
    // .../restconf/operational/network-topology:network-topology/topology/topology-netconf
    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID = InstanceIdentifier
            .create(NetworkTopology.class).child(
                    Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME
                            .getLocalName())));

    /**
     * Determines the Netconf Node Node ID, given the node's instance
     * identifier.
     *
     * @param path
     *            Node's instance identifier
     * @return NodeId for the node
     */
    private NodeId getNodeId(final InstanceIdentifier<?> path) {
        for (InstanceIdentifier.PathArgument pathArgument : path
                .getPathArguments()) {
            if (pathArgument instanceof InstanceIdentifier.IdentifiableItem<?, ?>) {

                final Identifier key = ((InstanceIdentifier.IdentifiableItem) pathArgument)
                        .getKey();
                if (key instanceof NodeKey) {
                    return ((NodeKey) key).getNodeId();
                }
            }
        }
        return null;
    }
    /**
     *
     * Initialize per node transaction.
     *
     * @param path IID for particular node
     * @return per node tx
     * @throws TxException.TxInitiatizationFailedException thrown when unable to initialize the tx
     */
    @Nonnull @Override public ReadWriteTransaction newTx(InstanceIdentifier<?> path) {
        ReadWriteTransaction delegate = null;
        DataBroker NodeBroker = null;
        NodeId nodeId = getNodeId(path);

        final Optional<MountPoint> mpNodeOptional = mountService
                .getMountPoint(NETCONF_TOPO_IID.child(Node.class,
                        new NodeKey(nodeId)));
        Preconditions
                .checkArgument(
                        mpNodeOptional.isPresent(),
                        "Unable to locate mountpoint: %s, not mounted yet or not configured",
                        nodeId.getValue());
        final MountPoint mpNode = mpNodeOptional.get();
        // Get the DataBroker for the mounted node
        NodeBroker = mpNode.getService(DataBroker.class).get();
        // Open an read and write transaction using the databroker.
        delegate = NodeBroker.newReadWriteTransaction();

        return delegate;
    }
}