import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author ImOkkkk
 * @date 2022/8/16 20:55
 * @since 1.0
 */
public class Main {
  public String OLD_ACCOUNT_COOKIE;

  public String NEW_ACCOUNT_COOKIE;

  public String NEW_ACCOUNT_TOKEN;

  public static void main(String[] args) throws InterruptedException {
    Main main = new Main();
    main.init();
    main.getFollowers();
    main.follow();
  }

  public void getFollowers() {
    List<String> uids = new ArrayList<>();
    helper(uids, 1);
    String path = ClassLoader.getSystemClassLoader().getResource("ids.txt").getPath();
    uids = uids.stream().distinct().collect(Collectors.toList());
    FileUtil.writeLines(uids, path, "UTF-8");
  }

  public void follow() throws InterruptedException {
    String path = ClassLoader.getSystemClassLoader().getResource("ids.txt").getPath();
    List<String> uids = FileUtil.readLines(path, "UTF-8");
    for (String uid : uids) {
      Thread.sleep(10000);
      HttpRequest post = HttpUtil.createPost("https://weibo.com/ajax/friendships/create");
      post.header("cookie", NEW_ACCOUNT_COOKIE)
          .header("x-xsrf-token", NEW_ACCOUNT_TOKEN)
          .header("accept", "application/json, text/plain, */*")
          .cookie(NEW_ACCOUNT_COOKIE);
      Body body = new Body(uid, "follows");
      post.body(JSON.toJSONString(body));
      HttpResponse response = post.execute();
      System.out.println("关注：" + JSON.parseObject(response.body()).getString("name") + "成功！");
    }
  }

  private void init() {
    Properties props = new Properties();
    InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
    try {
      props.load(inputStream);
      OLD_ACCOUNT_COOKIE = props.getProperty("old-account-cookie");
      NEW_ACCOUNT_COOKIE = props.getProperty("new-account-cookie");
      NEW_ACCOUNT_TOKEN = props.getProperty("new-account-token");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void helper(List<String> uids, int i) {
    HttpRequest get =
        HttpUtil.createGet(
            "https://weibo.com/ajax/profile/followContent?page=" + i + "&next_cursor=50");
    get.header("cookie", OLD_ACCOUNT_COOKIE).cookie(OLD_ACCOUNT_COOKIE);
    HttpResponse response = get.execute();
    JSONObject jsonObject = JSON.parseObject(response.body());
    JSONObject data = jsonObject.getJSONObject("data");
    JSONObject follows = data.getJSONObject("follows");
    JSONArray users = follows.getJSONArray("users");
    if (users != null && users.size() > 0) {
      for (Object o : users) {
        JSONObject parseObject = JSON.parseObject(o.toString());
        uids.add(parseObject.getString("id"));
      }
      helper(uids, i + 1);
    } else {
      return;
    }
  }

  public class Body {
    private String friend_uid;
    private String page;

    public Body(String friend_uid, String page) {
      this.friend_uid = friend_uid;
      this.page = page;
    }

    public String getFriend_uid() {
      return friend_uid;
    }

    public void setFriend_uid(String friend_uid) {
      this.friend_uid = friend_uid;
    }

    public String getPage() {
      return page;
    }

    public void setPage(String page) {
      this.page = page;
    }
  }
}
