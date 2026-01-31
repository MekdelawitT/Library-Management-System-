package library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for managing Member persistence.
 * SRP: Member data access only.
 * DIP: Delegates storage to DataHandler abstraction.
 */
public class MemberRepository {

    private final DataHandler<Member> dataHandler;

    public MemberRepository(DataHandler<Member> dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void save(Member member) {
        List<Member> all = new ArrayList<>(dataHandler.readData());

        // Remove existing instance if present
        all = all.stream()
                .filter(m -> m.getId() != member.getId())
                .collect(Collectors.toList());

        all.add(member);
        dataHandler.saveData(all);
    }

    public void deleteData(int id) {
        dataHandler.deleteData(id);
    }

    public List<Member> findAll() {
        return dataHandler.readData();
    }
}
