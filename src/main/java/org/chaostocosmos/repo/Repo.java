package org.chaostocosmos.repo;

/**
 * Repo class
 */
public class Repo {

    String id;
    String type;
    String url;

    /**
     * Constructor
     * @param id
     * @param type
     * @param url
     */
    public Repo(String id, String type, String url) {
        this.id = id;
        this.type = type;
        this.url = url;
    }

    /**
     * Get id
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get type
     * @return
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get url
     * @return
     */
    public String getUrl() {
        return this.url;
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", url='" + getUrl() + "'" +
            "}";
    }
}
