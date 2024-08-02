package OrderService;

import Common.Entity;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class UserProductPuchase  implements DataSerializable {
    private int product_id;
    private int purchased;

    public UserProductPuchase(int productId, int purchased) {
        product_id = productId;
        this.purchased = purchased;
    }
    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public int getPurchased() {
        return purchased;
    }

    public void setPurchased(int purchased) {
        this.purchased = purchased;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(product_id);
        out.writeInt(purchased);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        product_id = in.readInt();
        purchased = in.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProductPuchase that)) return false;
        return product_id == that.product_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(product_id);
    }
}
