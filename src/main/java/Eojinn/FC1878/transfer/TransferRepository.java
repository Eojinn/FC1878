package Eojinn.FC1878.transfer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findAllByOrderByTransferDateDescIdDesc();

    List<Transfer> findTop5ByPlayerNameEnIgnoreCaseOrderByTransferDateDesc(String playerNameEn);

    List<Transfer> findTop10ByPlayerNameContainingOrPlayerNameEnContainingIgnoreCaseOrderByTransferDateDesc(
            String playerName, String playerNameEn);
}
