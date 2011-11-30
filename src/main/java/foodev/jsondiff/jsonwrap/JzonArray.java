package foodev.jsondiff.jsonwrap;


public interface JzonArray extends JzonElement {

    int size();


    JzonElement get(int index);


    void addNull();


    void insert(int index, JzonElement el);


    void set(int index, JzonElement el);


    void remove(int index);

}
