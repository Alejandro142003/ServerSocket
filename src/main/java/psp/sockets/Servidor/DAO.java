package psp.sockets.Servidor;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Component;
import java.util.List;

@RequiredArgsConstructor
public class DAO {
    private final SessionFactory sessionFactory;

    // Método para guardar o actualizar un objeto en la base de datos
    public void guardar(Object objeto) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(objeto);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // Método para obtener un objeto por su ID
    public Object obtenerPorId(Class<?> clase, int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(clase, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para eliminar un objeto de la base de datos
    public void eliminar(Object objeto) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(objeto);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    // Método para obtener todos los objetos de una clase
    @SuppressWarnings("unchecked")
    public List<Object> obtenerTodos(Class<?> clase) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from " + clase.getName()).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para cerrar la sesión de Hibernate
    public void cerrar() {
        sessionFactory.close();
    }
}
