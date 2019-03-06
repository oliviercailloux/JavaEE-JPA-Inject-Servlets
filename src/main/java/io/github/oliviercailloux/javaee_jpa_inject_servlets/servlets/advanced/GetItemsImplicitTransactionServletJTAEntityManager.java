package io.github.oliviercailloux.javaee_jpa_inject_servlets.servlets.advanced;

import java.io.IOException;
import java.util.List;

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

import io.github.oliviercailloux.javaee_jpa_inject_servlets.model.Item;
import io.github.oliviercailloux.javaee_jpa_inject_servlets.utils.QueryHelper;
import io.github.oliviercailloux.javaee_jpa_inject_servlets.utils.ServletHelper;

@SuppressWarnings("serial")
@WebServlet("/getItemsImplicitTransactionServletJTAEntityManager")
public class GetItemsImplicitTransactionServletJTAEntityManager extends HttpServlet {
	@PersistenceContext
	private EntityManager em;

	@Inject
	private QueryHelper helper;

	@Inject
	private ServletHelper servletHelper;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		assert (!em.isJoinedToTransaction());

		@SuppressWarnings("resource")
		final ServletOutputStream out = servletHelper.configureAndGetOutputStream(resp);
		out.println("I use a container-managed JTA entity manager.");
		out.println(
				"My persistence context is transaction-scoped (lifetime scoped to a single transaction) and synchronised (joins automatically the current JTA transaction).");
		out.println("I use a no-lock read-only query with implicit transaction.");
		out.flush();

		final CriteriaQuery<Item> query = helper.selectAll(Item.class);
		final List<Item> allItems = em.createQuery(query).getResultList();
		assert (!em.isJoinedToTransaction());
		for (Item item : allItems) {
			assert (!em.contains(item));
		}

		for (Item item : allItems) {
			out.println(item.getName());
		}
	}
}
