package politicConnect.vote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

      List<Vote> findByVoteCategoryOrderByStartDateDesc(String category);
}
