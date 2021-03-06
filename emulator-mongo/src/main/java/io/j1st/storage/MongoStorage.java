

package io.j1st.storage;

import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import io.j1st.storage.entity.*;
import io.j1st.storage.utils.DateUtils;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

/**
 * MongoDB Storage
 */
public class MongoStorage {

    protected MongoClient client;
    protected MongoDatabase database;

    public void init(AbstractConfiguration config) {

        MongoClientOptions options = MongoClientOptions.builder().build();

        // MongoClient
        List<ServerAddress> addresses = parseAddresses(config.getString("mongo.address"));
        List<MongoCredential> credentials = parseCredentials(
                config.getString("mongo.userName"),
                "admin",
                config.getString("mongo.password"));
        if (addresses.size() == 1) {
            this.client = new MongoClient(addresses.get(0), credentials);
        } else {
            this.client = new MongoClient(addresses, credentials);
        }
        this.database = this.client.getDatabase(config.getString("mongo.database"));

        // indexes
        try {
            this.database.getCollection("users").dropIndex(ascending("name"));
        } catch (MongoCommandException e) {
            System.out.println("不存在的索引");
        }
//        this.database.getCollection("users").createIndex(ascending("name"), new IndexOptions().unique(true));
//        this.database.getCollection("users").createIndex(ascending("token"), new IndexOptions().unique(true));
//        this.database.getCollection("sms_verifies").createIndex(ascending("mobile"));
//        this.database.getCollection("sms_verifies").createIndex(ascending("updated_at"), new IndexOptions().expireAfter(1440L, TimeUnit.MINUTES));
//
//        this.database.getCollection("mail_verifies").createIndex(ascending("mail"));
//        this.database.getCollection("mail_verifies").createIndex(ascending("updated_at"), new IndexOptions().expireAfter(5L, TimeUnit.MINUTES));
//        this.database.getCollection("products").createIndex(ascending("token"), new IndexOptions().unique(true));
//        this.database.getCollection("products").createIndex(ascending("user_id"));
//        this.database.getCollection("products").createIndex(ascending("settings.fields.device_type", "settings.fields.key"));
//        this.database.getCollection("agents").createIndex(ascending("token"), new IndexOptions().unique(true));
//        this.database.getCollection("agents").createIndex(ascending("product_id"));
//        this.database.getCollection("agents").createIndex(ascending("permissions.user_id", "permissions.level"));
//        this.database.getCollection("devices").createIndex(ascending("agent_id", "sn"), new IndexOptions().unique(true));
//        this.database.getCollection("geetest_verifies").createIndex(ascending("datetime"), new IndexOptions().expireAfter(10L, TimeUnit.MINUTES));
//        this.database.getCollection("event_logs").createIndex(ascending("action_date"), new IndexOptions().expireAfter(15L, TimeUnit.DAYS));
//        this.database.getCollection("event_logs").createIndex(ascending("user_id", "product_id", "agent_id"));
//        // this.database.getCollection("emulator_datas").createIndex(ascending("test"), new IndexOptions().expireAfter(1L, TimeUnit.MINUTES));
        //drop index(这里暂时没用，预留，当我们需要改变与时间相关的检索字段时，需要先删除再新建，删除的前提是检索字段已经存在，不存在会报错，慎改)
        //this.database.getCollection("emulator_datas").dropIndex(ascending("test"));
        //this.database.getCollection("mail_verifies").dropIndex(ascending("mail"));
        //this.database.getCollection("users").dropIndex(ascending("email"));
        //this.database.getCollection("sms_verifies").dropIndex(ascending("updated_at"));
        //this.database.getCollection("sms_verifies").dropIndex(ascending("mobile"));
    }

    public void destroy() {
        if (this.client != null) this.client.close();
    }

    private ServerAddress parseAddress(String address) {
        int idx = address.indexOf(':');
        return (idx == -1) ?
                new ServerAddress(address) :
                new ServerAddress(address.substring(0, idx), Integer.parseInt(address.substring(idx + 1)));
    }

    private List<ServerAddress> parseAddresses(String addresses) {
        List<ServerAddress> result = new ArrayList<>();
        String[] addrs = addresses.split(" *, *");
        for (String addr : addrs) {
            result.add(parseAddress(addr));
        }
        return result;
    }

    private List<MongoCredential> parseCredentials(String userName, String database, String password) {
        List<MongoCredential> result = new ArrayList<>();
        //MongoCredential类的createCredential方法可以指定认证的用户名，密码，以及使用的数据库，并返回一个MongoCredential对象
        result.add(MongoCredential.createCredential(userName, database, password.toCharArray()));
        return result;
    }

    /*===============================================User Operations===========================================*/

    /**
     * 判断 用户名 是否存在
     *
     * @param name 用户名
     * @return True 如果存在
     */
    public boolean isUserNameExist(String name) {
        Document d = this.database.getCollection("users")
                .find(eq("name", name))
                .projection(include("_id"))
                .first();
        return d != null;
    }

    /**
     * 判断 用户邮箱 是否存在
     *
     * @param email 用户邮箱
     * @return True 如果存在
     */
    public boolean isUserEmailExist(String email) {
        Document d = this.database.getCollection("users")
                .find(eq("email", email))
                .projection(include("_id"))
                .first();
        return d != null;
    }

    /**
     * 获取 采集器，根据Id
     *
     * @param id 采集器Id
     * @return 采集器 or Null
     */
    public Agent getAgentById(ObjectId id) {
        Document d = this.database.getCollection("agents")
                .find(eq("_id", id))
                .first();
        if (d == null) return null;
        return parseAgentDocument(d);
    }

    /**
     * 判断 用户邮箱 是否存在
     *
     * @param email 用户邮箱
     * @param role  用户角色
     * @return True 如果存在
     */
    public boolean isUserEmailExist(String email, UserRole role) {
        Document d = this.database.getCollection("users")
                .find(and(eq("email", email), eq("role", role.value())))
                .projection(include("_id"))
                .first();
        return d != null;
    }

    /**
     * 判断 用户密码 是否正确
     *
     * @return True 密码正确、存在
     */
    public boolean isUserPasswordCorrect(ObjectId id, String password) {
        Document d = this.database.getCollection("users")
                .find(and(eq("_id", id), eq("password", password)))
                .projection(include("_id"))
                .first();
        return d != null;
    }


    /**
     * 判断 用户手机 是否存在
     *
     * @param mobile 用户手机
     * @return True 如果存在
     */
    public boolean isUserMobileExist(String mobile) {
        Document d = this.database.getCollection("users")
                .find(eq("mobile", mobile))
                .projection(include("_id"))
                .first();
        return d != null;
    }

    /**
     * 判断 用户手机 是否存在
     *
     * @param mobile 用户手机
     * @param role   用户角色
     * @return True 如果存在
     */
    public boolean isUserMobileExist(String mobile, UserRole role) {
        Document d = this.database.getCollection("users")
                .find(and(eq("mobile", mobile), eq("role", role.value())))
                .projection(include("_id"))
                .first();
        return d != null;
    }


    /**
     * 新增 用户
     * Token 在此处生成
     * 如果 用户名 邮箱 手机 已存在，会抛出异常
     * 调用方法应捕捉异常
     *
     * @param user 用户的信息
     * @return 用户（含Id）
     */
    public User insertUser(User user) {
        String token = RandomStringUtils.randomAlphabetic(32);
        Document d = new Document();
        d.append("name", user.getName());
        d.append("password", user.getPassword());
        d.append("token", token);
        if (user.getEmail() != null) {
            d.append("email", user.getEmail());
        }
        if (user.getMobile() != null) {
            d.append("mobile", user.getMobile());
        }

        d.append("role", user.getRole().value());
        d.append("updated_at", new Date()); // 当前版本 MongoDB insert 不支持试用 $currentDate
        this.database.getCollection("users").insertOne(d);
        user.setId(d.getObjectId("_id"));
        user.setToken(d.getString("token"));
        user.setUpdatedAt(d.getDate("updated_at"));
        return user;
    }

    /**
     * 更新 用户信息
     *
     * @param user 用户的信息
     * @return 用户（含Id）
     */
    public Boolean updateUserProfile(User user) {

        Document d = new Document();
        if (user.getName() != null) {
            d.append("name", user.getName());
        }
        if (user.getPassword() != null) {
            d.append("password", user.getPassword());
        }
        if (user.getCompany() != null) {
            d.append("company", user.getCompany());
        }
        if (user.getEmail() != null) {
            d.append("email", user.getEmail());
        }
        if (user.getMobile() != null) {
            d.append("mobile", user.getMobile());
        }
        d.append("updated_at", new Date());
        return this.database.getCollection("users").updateOne(eq("_id", user.getId()), new Document().append("$set", d)).getModifiedCount() > 0;
    }

    /**
     * 更新 用户密码
     *
     * @param user 用户的信息
     * @return 用户 是否验证成功
     */
    public Boolean updateUserPassword(User user) {
        Document d = new Document();
        d.append("password", user.getPassword());
        d.append("updated_at", new Date());
        return this.database.getCollection("users").updateOne(eq("token", user.getToken()), new Document().append("$set", d)).getModifiedCount() > 0;
    }

    /**
     * 找回密码
     *
     * @param user 用户的信息
     * @return 是否重置成功
     */
    public Boolean retrievePassword(User user) {
        Document d = new Document();
        d.append("mobile", user.getMobile());
        d.append("password", user.getPassword());
        d.append("updated_at", new Date());
        return this.database.getCollection("users").updateOne(eq("mobile", user.getMobile()), new Document().append("$set", d)).getModifiedCount() > 0;
    }

    /**
     * 获取 Token，根据 用户名 密码
     * 密码需要加密
     *
     * @param name     用户名
     * @param password 密码
     * @return Token or Null
     */
    public String getUserTokenByNamePassword(String name, String password) {
        Document d = this.database.getCollection("users")
                .find(and(eq("name", name), eq("password", password)))
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }

    /**
     * 获取 Token，根据 用户名 密码
     * 密码需要加密
     *
     * @param name     用户名
     * @param password 密码
     * @return Token or Null
     */
    public String getUserTokenByNamePasswordRole(String name, String password, UserRole role) {
        BasicDBObject query = new BasicDBObject();
        query.put("name", name);
        query.put("password", password);
        query.put("role", role.value());
        Document d = this.database.getCollection("users")
                .find(query)
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }


    /**
     * 获取 Token，根据 邮箱 密码
     * 密码需要加密
     *
     * @param email    邮箱
     * @param password 密码
     * @return Token or Null
     */
    public String getUserTokenByEmailPassword(String email, String password) {
        Document d = this.database.getCollection("users")
                .find(and(eq("email", email), eq("password", password)))
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }

    /**
     * 获取 Token，根据 邮箱 密码
     * 密码需要加密
     *
     * @param email    邮箱
     * @param password 密码
     * @param role     用户角色
     * @return Token or Null
     */
    public String getUserTokenByEmailPasswordRole(String email, String password, UserRole role) {
        BasicDBObject query = new BasicDBObject();
        query.put("email", email);
        query.put("password", password);
        query.put("role", role.value());
        Document d = this.database.getCollection("users")
                .find(query)
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }

    /**
     * 获取 Token，根据 手机
     *
     * @param mobile 手机
     * @return Token or Null
     */
    public String getUserTokenByMobile(String mobile) {
        Document d = this.database.getCollection("users")
                .find(eq("mobile", mobile))
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }

    /**
     * 获取 Token，根据 手机 用户角色
     *
     * @param mobile 手机
     * @param role   用户角色
     * @return Token or Null
     */
    public String getUserTokenByMobile(String mobile, UserRole role) {
        Document d = this.database.getCollection("users")
                .find(and(eq("mobile", mobile), eq("role", role.value())))
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }

    /**
     * 获取 Token，根据 邮箱
     *
     * @param mail 邮箱
     * @return Token or Null
     */
    public String getUserTokenByMail(String mail) {
        Document d = this.database.getCollection("users")
                .find(eq("mail", mail))
                .projection(include("token"))
                .first();
        if (d != null) {
            String token = "Token:" + d.getString("token");
            System.out.println("ttt" + token);
            return token;
        } else return null;
    }

    /**
     * 获取 Token，根据 邮箱 用户角色
     *
     * @param mail 邮箱
     * @param role 用户角色
     * @return Token or Null
     */
    public String getUserTokenByMail(String mail, UserRole role) {
        Document d = this.database.getCollection("users")
                .find(and(eq("mail", mail), eq("role", role.value())))
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }


    /**
     * 获取 Token，根据 手机 密码
     * 密码需要加密
     *
     * @param mobile   手机
     * @param password 密码
     * @return Token or Null
     */
    public String getUserTokenByMobilePassword(String mobile, String password) {
        Document d = this.database.getCollection("users")
                .find(and(eq("mobile", mobile), eq("password", password)))
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }

    /**
     * 获取 Token，根据 手机 密码
     * 密码需要加密
     *
     * @param mobile   手机
     * @param password 密码
     * @return Token or Null
     */
    public String getUserTokenByMobilePasswordRole(String mobile, String password, UserRole role) {
        BasicDBObject query = new BasicDBObject();
        query.put("mobile", mobile);
        query.put("password", password);
        query.put("role", role.value());
        Document d = this.database.getCollection("users")
                .find(query)
                .projection(include("token"))
                .first();
        if (d != null) return d.getString("token");
        else return null;
    }


    /**
     * 获取 用户信息，根据用户Id
     *
     * @param id 用户Id
     * @return 用户信息 or Null
     */
    public User getUserById(ObjectId id) {
        Document d = this.database.getCollection("agents")
                .find(eq("_id", id))
                .projection(include("product_id"))
                .first();
        ObjectId pid = d.getObjectId("product_id");

        Document products = this.database.getCollection("products")
                .find(eq("_id", pid))
                .projection(include("user_id"))
                .first();
        ObjectId uid = (ObjectId) products.get("user_id");

        Document user = this.database.getCollection("users")
                .find(eq("_id", uid))
                .projection(exclude("password"))
                .first();
        if (user == null) return null;
        return parseUserDocument(user);
    }

    /**
     * 获取 用户信息，根据 Token
     *
     * @param token Token
     * @return 用户信息 or Null
     */
    public User getUserByToken(String token) {
        Document d = this.database.getCollection("users")
                .find(eq("token", token))
                .projection(exclude("password"))
                .first();
        if (d == null) return null;
        return parseUserDocument(d);
    }

    private User parseUserDocument(Document d) {
        User u = new User();
        u.setId(d.getObjectId("_id"));
        u.setName(d.getString("name"));
        u.setToken(d.getString("token"));
        u.setEmail(d.getString("email"));
        u.setMobile(d.getString("mobile"));
        u.setCompany(d.getString("company"));
        u.setRole(UserRole.valueOf(d.getInteger("role")));
        u.setUpdatedAt(d.getDate("updated_at"));
        return u;
    }

    /**
     * 删除用户相关的所有信息
     */
    public String deleteBasicInfo(String userName, int role) {
        //获取用户
        Document document = this.database.getCollection("users").find(and(eq("name", userName), eq("role", role))).first();

        long deviceCount = 0, agentCount = 0, productCount = 0, logCount = 0, userCount = 0;
        if (document != null) {
            User user = parseUserDocument(document);

            if (user != null) {
                //获取product id
                List<Product> r = new ArrayList<>();
                this.database.getCollection("products").find(eq("user_id", user.getId())).forEach((Consumer<Document>) d ->
                        r.add(parseProductDocument(d)));

                List<ObjectId> obids = new ArrayList<>();
                for (Product aR : r) {
                    obids.add(aR.getId());
                }

                //删除device info
                deviceCount = this.database.getCollection("devices").deleteMany(in("product_id", obids)).getDeletedCount();

                //删除agent info
                agentCount = this.database.getCollection("agents").deleteMany(in("product_id", obids)).getDeletedCount();

                //删除product info
                productCount = this.database.getCollection("products").deleteMany(in("_id", obids)).getDeletedCount();

                //删除日志信息
                logCount = this.database.getCollection("event_logs").deleteMany(in("product_id", obids)).getDeletedCount();

                //删除用户信息
                userCount = this.database.getCollection("users").deleteMany(and(eq("name", userName), eq("role", role))).getDeletedCount();

            }
        }
        return "成功删除: " + deviceCount + " 条device数据 " + agentCount + " 条agent数据 " + productCount + " 条product数据 " + logCount + " 条日志数据 " + userCount + " 条用户信息";

    }



    /*===============================================SMS Operations===========================================*/

    /**
     * 更新或新增 短信验证码
     *
     * @param mobile     手机号码
     * @param type       验证类型
     * @param verifyCode 验证码
     * @return True 成功更新
     */
    public boolean updateSmsVerifyCode(String mobile, VerifyType type, String verifyCode) {
        Document d = new Document();
        d.append("mobile", mobile);
        d.append("type", type.value());
        d.append("verify_code", verifyCode);
        return this.database.getCollection("sms_verifies")
                .updateOne(and(eq("mobile", mobile), eq("type", type.value())),
                        new Document()
                                .append("$set", d)
                                .append("$currentDate", new Document("updated_at", true)),
                        new UpdateOptions().upsert(true)).getModifiedCount() > 0;
    }

    /**
     * 判断 短信验证码 是否有效
     *
     * @param mobile     手机号码
     * @param type       验证类型
     * @param verifyCode 验证码
     * @return True 有效
     */
    public boolean isSmsVerifyCodeValid(String mobile, VerifyType type, String verifyCode) {
        return this.database.getCollection("sms_verifies")
                .find(and(eq("mobile", mobile), eq("type", type.value()), eq("verify_code", verifyCode)))
                .projection(include("_id"))
                .first() != null;
    }

    /**
     * 判断 用户注册码 是否过期 根据手机号
     *
     * @param mobile 用户手机
     * @return True 如果存在
     */
    public boolean verifyCodeExistByMobile(String mobile) {
        Document d = this.database.getCollection("sms_verifies")
                .find(eq("mobile", mobile))
                .projection(include("_id"))
                .first();
        return d != null;
    }



    /*===============================================Product Operations===========================================*/

    /**
     * 获取 产品，根据产品Id
     *
     * @param id 产品Id
     * @return 产品 or Null
     */
    public Product getProductById(ObjectId id) {
        Document d = this.database.getCollection("products")
                .find(eq("_id", id))
                .first();
        if (d == null) return null;
        return parseProductDocument(d);
    }

    /**
     * 获取 产品，根据Token
     *
     * @param token Token
     * @return 产品 or Null
     */
    public Product getProductByToken(String token) {
        Document d = this.database.getCollection("products")
                .find(eq("token", token))
                .first();
        if (d == null) return null;
        return parseProductDocument(d);
    }

    /**
     * 获取 产品列表，根据用户Id
     *
     * @param userId 用户Id
     * @return 产品列表 or Empty List
     */
    public List<Product> getProductsByUserId(ObjectId userId, int skip, int limit, boolean isAsc) {
        List<Product> r = new ArrayList<>();
        this.database.getCollection("products")
                .find(eq("user_id", userId))
                .sort(isAsc ? ascending("_id") : descending("_id"))
                .skip(skip)
                .limit(limit)
                .forEach((Consumer<Document>) d ->
                        r.add(parseProductDocument(d)));
        return r;
    }

    @SuppressWarnings("unchecked")
    private Product parseProductDocument(Document d) {
        Product p = new Product();
        p.setId(d.getObjectId("_id"));
        p.setUserId(d.getObjectId("user_id"));
        p.setName(d.getString("name"));
        p.setToken(d.getString("token"));
        p.setStatus(ProductStatus.valueOf(d.getInteger("status")));
        p.setScopes(ProductScopes.valueOf(d.getInteger("scopes")));
        Document ds = d.get("settings", Document.class);
        if (ds != null) {
            ProductSettings s = new ProductSettings();
            s.setTimezone(DateTimeZone.forID(ds.getString("timezone")));
            p.setSettings(s);
        }
        List<Document> fnButtonDocs = d.get("fnx", List.class);
        List<FnButton> fnButtons = new ArrayList<>();
        if (fnButtonDocs != null) {
            for (Document fnButtonDoc : fnButtonDocs) {
                FnButton fnButton = new FnButton();
                fnButton.setId(fnButtonDoc.getString("id"));
                fnButton.setCode(fnButtonDoc.get("code"));
                fnButton.setAlias(fnButtonDoc.getString("alias"));
                fnButton.setComment(fnButtonDoc.getString("comment"));
                fnButton.setType(fnButtonDoc.getInteger("type"));
                fnButton.setCreateDate(fnButtonDoc.getDate("createDate"));
                fnButton.setUpdatedDate(fnButtonDoc.getDate("updatedDate"));
                List<Document> paramDocs = fnButtonDoc.get("keys", List.class);
                if (paramDocs != null) {
                    List<FnKeyItem> items = new ArrayList<>();
                    for (Document paramDoc : paramDocs) {
                        FnKeyItem keyItem = new FnKeyItem();
                        keyItem.setKey(paramDoc.getString("key"));
                        keyItem.setAlias(paramDoc.getString("alias"));
                        keyItem.setDataType(paramDoc.getInteger("dataType"));
                        keyItem.setMethod(paramDoc.getInteger("method"));
                        keyItem.setDefaultValue(paramDoc.get("defaultValue"));
                        keyItem.setValueType(paramDoc.getInteger("valueType"));
                        keyItem.setValue(paramDoc.get("value"));
                        keyItem.setUnit(paramDoc.getString("unit"));
                        keyItem.setValid((Map<String, Object>) paramDoc.get("valid"));
                        items.add(keyItem);
                    }
                    fnButton.setKeys(items);
                }
                fnButtons.add(fnButton);
            }
        }
        p.setFnx(fnButtons);
        p.setUpdatedAt(d.getDate("updated_at"));
        return p;
    }

    /**
     * 获取 产品数量，根据用户Id
     *
     * @param userId 用户Id
     * @return 产品数量
     */
    public long getProductsCountByUserId(ObjectId userId) {
        return this.database.getCollection("products")
                .count(eq("user_id", userId));
    }

    /**
     * 获取 采集器列表，根据产品Id
     *
     * @param products_id 产品id
     * @return 采集器的列表 Or Empty List
     */
    public List<Agent> getAgentsByProductId(ObjectId products_id) {
        List<Agent> r = new ArrayList<>();
        this.database.getCollection("agents")
                .find(eq("product_id", products_id)
                )
                .forEach((Consumer<Document>) d ->
                        r.add(parseAgentDocument(d)));
        return r;
    }

//    public List<Agent> getAgentsByProductId(ObjectId products_id) {
//        List<Agent> r = new ArrayList<>();
//        this.database.getCollection("agents")
//                .find(eq("product_id", products_id)
//                )
//                .forEach((Consumer<Document>) d ->
//                        r.add(parseAgentDocument(d)));
//        return r;
//    }

    /**
     * 获取 采集器列表，根据产品Id
     *
     * @param id agentid
     * @return 采集器的列表 Or Empty List
     */
    public Agent getAgentsById(ObjectId id) {
        return parseAgentDocument(this.database.getCollection("agents")
                .find(eq("_id", id)
                ).first());
    }

    /**
     * 获取 采集器列表，根据产品Id
     *
     * @param productIds 产品Id列表
     * @param skip       分页起始（略过）
     * @param limit      分页数量
     * @param isAsc      升序？
     * @return 采集器列表 or Empty List
     */
    public List<Agent> getAgentsByProductIds(List<ObjectId> productIds, int skip, int limit, boolean isAsc) {
        List<Agent> r = new ArrayList<>();
        this.database.getCollection("agents")
                .find(in("product_id", productIds))
                .sort(isAsc ? ascending("_id") : descending("_id"))
                .skip(skip)
                .limit(limit)
                .forEach((Consumer<Document>) d ->
                        r.add(parseAgentDocument(d)));
        return r;
    }

    /**
     * 获取 采集器数量，根据产品Id
     *
     * @param productIds 产品Id列表
     * @return 采集器数量
     */
    public long getAgentsCountByProductId(List<ObjectId> productIds) {
        return this.database.getCollection("agents")
                .count(in("product_id", productIds));
    }

    /**
     * 获取 采集器列表，根据产品Id
     *
     * @param productIds  产品Id列表
     * @param isActivated 产品是否被激活
     * @param skip        分页起始（略过）
     * @param limit       分页数量
     * @param isAsc       升序？
     * @return 采集器列表 or Empty List
     */
    public List<Agent> getAgentsByProductIds(List<ObjectId> productIds, boolean isActivated, int skip, int limit, boolean isAsc) {
        List<Agent> r = new ArrayList<>();
        this.database.getCollection("agents")
                .find(and(in("product_id", productIds), exists("activated_at", isActivated)))
                .sort(isAsc ? ascending("_id") : descending("_id"))
                .skip(skip)
                .limit(limit)
                .forEach((Consumer<Document>) d ->
                        r.add(parseAgentDocument(d)));
        return r;
    }

    /**
     * 获取 采集器数量，根据产品Id
     *
     * @param productIds  产品Id列表
     * @param isActivated 产品是否被激活
     * @return 采集器数量
     */
    public long getAgentsCountByProductId(List<ObjectId> productIds, boolean isActivated) {
        return this.database.getCollection("agents")
                .count(and(in("product_id", productIds), exists("activated_at", isActivated)));
    }

    /**
     * 获取 采集器列表，根据权限
     * 默认按创建时间排序
     *
     * @param permission 最低权限
     * @return 采集器列表 or Empty List
     */
    public List<Agent> getAgentsByPermission(Permission permission) {
        List<Agent> r = new ArrayList<>();
        this.database.getCollection("agents")
                .find(eq("permissions", new Document("$elemMatch", new Document()
                        .append("user_id", permission.getUserId())
                        .append("level", new Document("$gte", permission.getLevel().value())))))
                .sort(ascending("_id"))
                .forEach((Consumer<Document>) d ->
                        r.add(parseAgentDocument(d)));
        return r;
    }


    /**
     * 获取 采集器列表，根据权限
     * 默认按创建时间排序
     *
     * @param permission 最低权限
     * @param productId
     * @return 采集器列表 or Empty List
     */
    public List<Agent> getAgentsByPermissionAndProductId(Permission permission, ObjectId productId) {
        List<Agent> r = new ArrayList<>();
        this.database.getCollection("agents")
                .find(and(eq("product_id", productId), eq("permissions", new Document("$elemMatch", new Document()
                        .append("user_id", permission.getUserId())
                        .append("level", new Document("$gte", permission.getLevel().value()))))))
                .sort(ascending("_id"))
                .forEach((Consumer<Document>) d ->
                        r.add(parseAgentDocument(d)));
        return r;
    }


    /**
     * 获取 采集器的分组数据统计，根据产品分组
     *
     * @param productIds 产品Id列表
     * @return 数据统计
     */
    public AggregateIterable<Document> getAgentsGroupByProductId(List<ObjectId> productIds) {
        return this.database.getCollection("agents")
                .aggregate(Arrays.asList(
                        match(in("product_id", productIds)),
                        group("$product_id",
                                sum("total", 1),
                                sum("activated", new Document("$cond", Arrays.asList(new Document("$gte", Arrays.asList("$activated_at", new Date(0L))), 1, 0))),
                                sum("connected", new Document("$cond", Arrays.asList("$connected", 1, 0))))
                ));
    }

    @SuppressWarnings("unchecked")
    protected Agent parseAgentDocument(Document d) {
        Agent a = new Agent();
        a.setId(d.getObjectId("_id"));
        a.setProductId(d.getObjectId("product_id"));
        a.setName(d.getString("name"));
        a.setToken(d.getString("token"));
        a.setConnected(d.getBoolean("connected"));
        a.setMsgCount(d.getLong("msg_count"));
        a.setMsgSizeSum(d.getLong("msg_size_sum"));
        a.setAttributes(d.get("attributes", Map.class));
        List<Document> dps = d.get("permissions", List.class);
        if (dps != null) {
            List<Permission> permissions = new ArrayList<>();
            for (Document dp : dps) {
                Permission p = new Permission();
                p.setUserId(dp.getObjectId("user_id"));
                p.setLevel(PermissionLevel.valueOf(dp.getInteger("level")));
                permissions.add(p);
            }
            a.setPermissions(permissions);
        }
        a.setActivatedAt(d.getDate("activated_at"));
        a.setUpdatedAt(d.getDate("updated_at"));
        return a;
    }

    /**
     * 批量新增 采集器
     * Token 在此处生成
     *
     * @param productId 产品Id
     * @param count     新增数量
     * @return 新增采集器Id列表
     */
    public List<ObjectId> insertAgents(ObjectId productId, int count) {
        List<ObjectId> r = new ArrayList<>();
        List<Document> agents = new ArrayList<>();
        Date date = new Date();
        for (int i = 1; i <= count; i++) {
            String token = RandomStringUtils.randomAlphabetic(32);
            Document d = new Document();
            d.append("product_id", productId);
            d.append("token", token);
            d.append("connected", false);
            d.append("msg_count", 0L);
            d.append("msg_size_sum", 0L);
            d.append("permissions", new ArrayList<Document>());
            d.append("updated_at", date); // 当前版本 MongoDB insert 不支持试用 $currentDate
            agents.add(d);
        }
        this.database.getCollection("agents").insertMany(agents);
        agents.forEach(d -> r.add(d.getObjectId("_id")));
        return r;
    }

    /**
     * 删除 采集器，根据Id
     *
     * @return 删除数量
     */
    public long deleteemulatorRegisterById(String id) {
        return this.database.getCollection("emulator_register")
                .deleteOne(eq("agent_id", id))
                .getDeletedCount();
        // TODO: 删除相关设备和Metrics
    }

    /**
     * 更新 采集器 名称
     *
     * @param id   采集器Id
     * @param name 采集器名称
     * @return True 成功更新
     */
    public boolean updateAgentName(ObjectId id, String name) {
        return this.database.getCollection("agents")
                .updateOne(eq("_id", id),
                        new Document()
                                .append("$set", new Document("name", name))
                                .append("$currentDate", new Document("updated_at", true)),
                        new UpdateOptions().upsert(false)).getModifiedCount() > 0;
    }

    /**
     * 更新 采集器 连接状态
     *
     * @param id        采集器Id
     * @param connected 采集器连接状态
     * @return True 成功更新
     */
    public boolean updateAgentConnected(ObjectId id, boolean connected) {
        if (connected) {
            this.database.getCollection("agents")
                    .updateOne(and(eq("_id", id), exists("activated_at", false)),
                            new Document("$currentDate", new Document("activated_at", true)),
                            new UpdateOptions().upsert(false));
        }

        return this.database.getCollection("agents")
                .updateOne(eq("_id", id),
                        new Document()
                                .append("$set", new Document("connected", connected))
                                .append("$currentDate", new Document("updated_at", true)),
                        new UpdateOptions().upsert(false)).getModifiedCount() > 0;
    }

    /**
     * 获取 产品 是否被激活
     * 激活的定义为：旗下采集器至少有一个被激活
     *
     * @param productId 产品Id
     * @return True 被激活
     */
    public boolean isProductActivated(ObjectId productId) {
        return this.database.getCollection("agents")
                .find(and(eq("product_id", productId), exists("activated_at", true)))
                .projection(include("_id"))
                .first() != null;

    }


    /**
     * 删除time时间之前数据
     *
     * @param time 时间
     * @return 受影响行数
     */
    public long deleteDataByTime(String time, int is) {
        FindIterable<Document> TDocument = database.getCollection("emulator_datas").find();
        long count = 0;
        for (Document d : TDocument
                ) {
            if (Double.parseDouble(d.get("time").toString()) > Double.parseDouble(time) && is > 0
                    || is == 0 && Double.parseDouble(d.get("time").toString()) < Double.parseDouble(time)) {
                count += this.database.getCollection("emulator_datas")
                        .deleteOne(eq("time", d.get("time")))
                        .getDeletedCount();
            }
        }

        return count;
    }

    /**
     * updateEmulatorRegister
     *
     * @param agentId agentId
     * @param key     key
     * @param value   value
     * @return
     */
    public boolean updateEmulatorRegister(String agentId, String key, Object value) {
        if (key.equals("onlinefail"))
            return this.database.getCollection("emulator_register")
                    .updateOne(eq("agent_id", agentId),
                            new Document("$set", new Document(key, value)
                            )
                            , new UpdateOptions().upsert(true)).getModifiedCount() > 0;
        else
            return this.database.getCollection("emulator_register")
                    .updateOne(eq("agent_id", agentId),
                            new Document("$set", new Document(key, value)
                                    .append("updated_at", new Date()))
                            , new UpdateOptions().upsert(true)).getModifiedCount() > 0;
    }

    /**
     * 根据plantId查询Plant下的资产信息列表
     *
     * @param plantId
     * @return
     */
    public List<AssetsInfo> getAssetsListByPlantId(ObjectId plantId, List<String> tags, Integer assetType) {
        List<AssetsInfo> AssetsInfos = new ArrayList<>();
        Document query = new Document();
        query.append("plant_id", plantId);
        if (assetType != null) {
            query.append("assets_type", assetType);
        }
        if (tags != null && tags.size() > 0) {
            query.append("tags", new Document("$in", tags));
        }
        this.database.getCollection("user_assets_info")
                .find(query)
                .forEach((Consumer<Document>) document ->
                        AssetsInfos.add(parseAssets(document)));
        return AssetsInfos;
    }

    /**
     * 解析资产组文档信息
     *
     * @param doc 查询出来的资产组文档
     * @return 资产组信息
     */
    public static AssetsInfo parseAssets(Document doc) {
        AssetsInfo assetsInfo = new AssetsInfo();
        assetsInfo.setId(doc.getObjectId("_id"));
        assetsInfo.setAgentId(doc.get("agent_id") != null ? doc.getObjectId("agent_id") : null);
        assetsInfo.setUserId(doc.get("user_id") != null ? doc.getObjectId("user_id") : null);
        assetsInfo.setGroupId(doc.get("group_id") != null ? doc.getObjectId("group_id") : null);
        assetsInfo.setSystemId(doc.get("system_id") != null ? doc.getObjectId("system_id") : null);
        assetsInfo.setPlantId(doc.get("plant_id") != null ? doc.getObjectId("plant_id") : null);
        assetsInfo.setTags((List<String>) doc.get("tags"));
        assetsInfo.setSn(doc.getString("sn"));
        assetsInfo.setType(doc.getString("type"));
        assetsInfo.setProductName(doc.getString("product_name"));
        assetsInfo.setStatus(doc.getInteger("status"));
        assetsInfo.setCreatedAt(doc.getDate("created_at"));
        assetsInfo.setUpdatedAt(doc.getDate("updated_at"));
        return assetsInfo;
    }

    /**
     * findEmulatorRegister
     *
     * @param key     key
     * @param agentId agentId
     * @return
     */
    public Object findEmulatorRegister(String agentId, String key) {
        Object data;
        try {
            data = this.database.getCollection("emulator_register")
                    .find(eq("agent_id", agentId)).first().get(key);
        } catch (NullPointerException e) {
            data = null;
        }
        return data;
    }

    /**
     * 查询正在运行任务总数
     *
     * @return
     */
    public long findEmulatorJobNum() {
        long num;
        try {
            num = this.database.getCollection("emulator_register").count(eq("onlinefail", 1));
        } catch (NullPointerException e) {
            return 0;
        }
        return num;
    }


    public Document findEmulatorDate(String agentId) {
        Document data;
        try {
            data = this.database.getCollection("emulator_register")
                    .find(eq("agent_id", agentId)).first();
        } catch (NullPointerException e) {
            data = null;
        }
        return data;
    }

    /**
     * 查找启动Agent
     *
     * @param onlinefail 需要上传在线的
     * @return agent 集合
     */
    public List<EmulatorRegister> findEmulatorAgentInfoBy(int onlinefail) {
        List<EmulatorRegister> agentidall = new ArrayList<>();
        this.database.getCollection("emulator_register")
                .find(eq("onlinefail", onlinefail)).forEach((Consumer<Document>) document ->
                agentidall.add(parseRegisterDocument(document))
        );
        return agentidall;
    }

    /**
     * 查找单个Agent或批次看是否满足启动情况
     *
     * @param id         id任务
     * @param onlinefail 是否运行
     * @param systemTpye 数据系统类型
     * @param agentType  id类型
     * @return 满足条件
     */
    public boolean isEmulatorAgentInfoBy(String id, int onlinefail, int systemTpye, int agentType) {
        boolean is;
        List<Integer> onlinefailAll = new ArrayList<>();
        int acunt = 0;
        if (agentType == 0)//agentid
        {
            is = this.database.getCollection("emulator_register")
                    .find(and(eq("agent_id", id),
                            eq("onlinefail", onlinefail),
                            eq("systemTpye", systemTpye))).first() == null;

        } else {//batch id
            this.database.getCollection("emulator_register")
                    .find(and(eq("product_id", id),
                            eq("onlinefail", onlinefail),
                            eq("systemTpye", systemTpye))).forEach((Consumer<Document>) document ->
                    onlinefailAll.add(isnoDocument(document)));
            for (Integer a : onlinefailAll) {
                acunt += a;
            }
            is = acunt == 0;
        }
        return is;
    }

    @SuppressWarnings("unchecked")
    protected int isnoDocument(Document d) {
        return d.getInteger("onlinefail");
    }

    /**
     * 获取 模拟器信息
     *
     * @param skip  分页起始（略过）
     * @param limit 分页数量
     * @param isAsc 创建时间升序？
     * @return 事件列表
     */
    public List<EmulatorRegister> getEmulatorRegisterByno(String id, int skip, int limit, boolean isAsc) {
        List<EmulatorRegister> r = new ArrayList<>();
        Pattern pattern = Pattern.compile("^.*" + id + ".*$", Pattern.CASE_INSENSITIVE);
        BasicDBObject query = new BasicDBObject();
        query.put("agent_id", pattern);
        query.put("onlinefail",1);
        this.database.getCollection("emulator_register")
                .find(query)
                .sort(isAsc ? ascending("created_at") : descending("created_at"))
                .skip((skip - 1) * limit).limit(limit)
                .forEach((Consumer<Document>) document ->
                        r.add(parseRegisterDocument(document)));
        return r;
    }

    //查询警告信息已读未读总数
    public Long getEmulatorRegister(String id) {
        Pattern pattern = Pattern.compile("^.*" + id + ".*$", Pattern.CASE_INSENSITIVE);
        BasicDBObject query = new BasicDBObject();
        query.put("agent_id", pattern);
        return this.database.getCollection("emulator_register").count(query);
    }

    @SuppressWarnings("unchecked")
    protected EmulatorRegister parseRegisterDocument(Document d) {
        try {
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmm");//可以方便地修改日期格式
            int odldate = Integer.parseInt(dateFormat.format(d.getDate("updated_at")));
            int newdate = Integer.parseInt(dateFormat.format(new Date()));
            EmulatorRegister e = new EmulatorRegister();
            e.setId(d.getObjectId("_id").toString());
            e.setAgent_id(d.getString("agent_id"));
            e.setUpdated_at(dateFormat1.format(d.getDate("updated_at")));
            e.setPacking(d.getString("packing"));
            e.setSystemType(d.getInteger("systemTpye"));
            e.setTopic(d.getString("topic"));
            e.setCreated_at(dateFormat1.format(d.getDate("created_at")));


            if (d.get("product_id") != null)
                e.setProdactId("product_id");
            else if (d.get("Plant_id") != null)
                e.setProdactId("Plant_id");
            //如果更新时间超时设为false
            if (newdate - odldate > 5) {
                e.setConnected(false);
                // updateEmulatorRegister(d.getString("agent_id"), "onlinefail", 0);
            } else {
                if (d.get("onlinefail") == null)
                    e.setConnected(false);
                else {
                    e.setConnected(d.getInteger("onlinefail") > 0);
                }
            }
            return e;
        } catch (NullPointerException e) {
            return null;
        }


    }
}
