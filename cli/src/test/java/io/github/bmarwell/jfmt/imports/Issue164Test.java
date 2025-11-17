package io.github.bmarwell.jfmt.imports;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class Issue164Test extends ImportOrderProcessorTestBase {

    @BeforeAll
    static void loadSource() {
        source = ImportOrderTestUtil.loadTestResource("imports/Issue164TestDatasource.java");
    }

    @Override
    protected String getProfileName() {
        return "defaultorder";
    }

    @Test
    void is_correctly_formatted() {
        // given
        String expected = """
                          import invalid.tool.dao.JpaUserDao;
                          import invalid.tool.dao.UserDao;
                          import jakarta.persistence.EntityManager;
                          import jakarta.persistence.EntityTransaction;
                          import jakarta.persistence.Parameter;
                          import jakarta.persistence.Query;
                          import jakarta.persistence.TypedQuery;
                          import jakarta.persistence.criteria.CriteriaQuery;
                          import java.util.function.Supplier;
                          import org.eclipse.persistence.internal.helper.DatabaseField;
                          import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
                          import org.eclipse.persistence.jpa.JpaEntityManager;
                          import org.eclipse.persistence.queries.DatabaseQuery;
                          import org.eclipse.persistence.sessions.DatabaseRecord;
                          import org.eclipse.persistence.sessions.Session;""";

        // when
        String actual = runAndGetImportBlock();

        // then
        assertTrue(actual.startsWith(expected), "Import block should have been as expected but was: " + actual);
    }
}
