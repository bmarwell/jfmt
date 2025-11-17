package invalid.tool.test.junit5;

import invalid.tool.dao.UserDao;
import invalid.tool.dao.JpaUserDao;

import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.DatabaseRecord;
import org.eclipse.persistence.sessions.Session;

import java.util.function.Supplier;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;

final class Issue164TestDatasource implements TestDataSource {
    @Override
    public <RET> RET executeInTransaction(final Supplier<RET> function) {
        return executeInTransaction(function, this.em);
    }

    @Override
    public void executeInTransaction(final Runnable function) {
        throw new UnsupportedOperationException("removed for testing.");
    }

    private static <RET> RET executeInTransaction(final Supplier<RET> function, final EntityManager entityManager) {
        throw new UnsupportedOperationException("removed for testing.");
    }

}
