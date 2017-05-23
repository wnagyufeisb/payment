package noumena.payment.model;



/**
 * CreateOrderId generated by MyEclipse - Hibernate Tools
 */

public class CreateOrderId  implements java.io.Serializable {


    // Fields    

     private Integer id;
     private String createTime;


    // Constructors

    /** default constructor */
    public CreateOrderId() {
    }

	/** minimal constructor */
    public CreateOrderId(Integer id) {
        this.id = id;
    }
    
    /** full constructor */
    public CreateOrderId(Integer id, String createTime) {
        this.id = id;
        this.createTime = createTime;
    }

   
    // Property accessors

    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public String getCreateTime() {
        return this.createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
   








}