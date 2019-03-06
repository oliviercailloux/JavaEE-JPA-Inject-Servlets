package io.github.oliviercailloux.javaee_jpa_inject_servlets.servlets;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.oliviercailloux.javaee_jpa_inject_servlets.model.Item;
import io.github.oliviercailloux.javaee_jpa_inject_servlets.service.ItemService;
import io.github.oliviercailloux.javaee_jpa_inject_servlets.utils.ServletHelper;

@SuppressWarnings("serial")
@WebServlet("/getItemsServletJTAEntityManager")
public class GetItemsServletJTAEntityManager extends HttpServlet {
	@PersistenceContext
	private EntityManager em;
	@Inject
	private ItemService itemS;
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
		out.println("I use a CDI interceptor for managing the transaction.");
		out.flush();

		final List<Item> allItems = itemS.getAll();
		assert (!em.isJoinedToTransaction());
		for (Item item : allItems) {
			assert (!em.contains(item));
		}

		for (Item item : allItems) {
			out.println(item.getName());
		}
		out.println("End.");
	}
}
