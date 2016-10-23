package cricketskill.io;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


class DynamoStore {
  private final DynamoDB _dynamoDB;
  private final String _tableName;
  private final Table _table;
  private final AmazonDynamoDBClient _client;

  // use HTTPS if you are testing locally
  protected DynamoStore(Protocol protocol, String tableName) {
    this._client = new AmazonDynamoDBClient(new ClientConfiguration().withProtocol(protocol));

    this._dynamoDB = new DynamoDB(_client);
    this._tableName = tableName;
    this._table = _dynamoDB.getTable(tableName);
  }

  protected Item get(GetItemSpec spec) {
    return _table.getItem(spec);
  }

  protected void put(Item i) {
    _table.putItem(i);
  }

  protected <T> List<Item> batchGet(Set<T> primaryKeys, String primaryKeyName) {

    if (primaryKeys.isEmpty()) {
      return Lists.newArrayList();
    }
    
    TableKeysAndAttributes batchGetAttributes = new TableKeysAndAttributes(_tableName);

    primaryKeys.stream()
        .map(i -> new KeyAttribute(primaryKeyName, i))
        .map(PrimaryKey::new)
        .forEach(batchGetAttributes::addPrimaryKey);

    return _dynamoDB.batchGetItem(batchGetAttributes)
        .getTableItems()
        .getOrDefault(_tableName, Lists.newArrayList());
  }

  protected <T> List<T> query(DynamoDBQueryExpression<T> expression, Class<T> clazz) {

    return new DynamoDBMapper(_client).query(clazz, expression);
  }

  protected <T> List<T> scan(DynamoDBScanExpression expression, Class<T> clazz) {
    return new DynamoDBMapper(_client).scan(clazz, expression)
        .stream()
        .collect(Collectors.toList());
  }
}
