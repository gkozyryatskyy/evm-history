package io.evm.history.service.model;

import lombok.Getter;
import lombok.Setter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class BlockAndReceiptsWrapper extends BlockWrapper {

    // ordered map for LogProducer produce logs in the initial order
    // TransactionIndexRaw to TransactionReceipt
    private Map<String, TransactionReceipt> receipts; //TODO switch to List with txIndex as index?

    public BlockAndReceiptsWrapper(EthBlock.Block block) {
        super(block);
    }

    public static Map<String, TransactionReceipt> mapReceipts(List<TransactionReceipt> receipts) {
        return mapReceipts(receipts.stream());
    }

    public static Map<String, TransactionReceipt> mapReceipts(Stream<TransactionReceipt> receipts) {
        return receipts.filter(Objects::nonNull)
                // use LinkedHashMap to save the order
                .collect(Collectors.toMap(TransactionReceipt::getTransactionIndexRaw, e -> e, (e1, e2) -> e1, LinkedHashMap::new));
    }

}
