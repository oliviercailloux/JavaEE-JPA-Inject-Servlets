package io.github.oliviercailloux.javaee_jpa_inject_servlets.servlets.advanced;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import io.github.oliviercailloux.javaee_jpa_inject_servlets.model.Item;
import io.github.oliviercailloux.javaee_jpa_inject_servlets.utils.QueryHelper;
import io.github.oliviercailloux.javaee_jpa_inject_servlets.utils.ServletHelper;

@SuppressWarnings("serial")
@WebServlet("/getItemsManualTransactionServletJTAEntityManager")
public class GetItemsManualTransactionServletJTAEntityManager extends HttpServlet {
	@PersistenceContext
	private EntityManager em;

	@Inject
	private QueryHelper helper;

	@Inject
	private ServletHelper servletHelper;

	@Resource
	private UserTransaction ut;

	@Override
	@SuppressWarnings("resource")
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			assert (ut.getStatus() == Status.STATUS_NO_TRANSACTION);
		} catch (SystemException e) {
			throw new IllegalStateException(e);
		}
		assert (!em.isJoinedToTransaction());

		final ServletOutputStream out = servletHelper.configureAndGetOutputStream(resp);
		out.println("I use a container-managed JTA entity manager.");
		out.println(
				"My persistence context is transaction-scoped (lifetime scoped to a single transaction) and synchronised (joins automatically the current JTA transaction).");
		out.flush();

		final List<Item> allItems;
		try {
			ut.begin();
			assert (em.isJoinedToTransaction());
			final CriteriaQuery<Item> query = helper.selectAll(Item.class);
			allItems = em.createQuery(query).getResultList();
			for (Item item : allItems) {
				assert (em.contains(item));
			}
			ut.commit();
		} catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | NotSupportedException
				| SystemException e) {
			throw new IllegalStateException(e);
		}

		try {
			assert (ut.getStatus() == Status.STATUS_NO_TRANSACTION);
		} catch (SystemException e) {
			throw new IllegalStateException(e);
		}
		assert (!em.isJoinedToTransaction());
		/** Still “open”, but entities are detached. */
		assert (em.isOpen());
		for (Item item : allItems) {
			assert (!em.contains(item));
		}

		for (Item item : allItems) {
			out.println(item.getName());
		}
	}
}
