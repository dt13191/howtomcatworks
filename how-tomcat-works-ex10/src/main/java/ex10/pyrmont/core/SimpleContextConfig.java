/* modify this to include authenticatorConfig method */
package ex10.pyrmont.core;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.deploy.LoginConfig;

public class SimpleContextConfig implements LifecycleListener {

  private Context context;
  public void lifecycleEvent(LifecycleEvent event) {
    if (Lifecycle.START_EVENT.equals(event.getType())) {
      context = (Context) event.getLifecycle();
      authenticatorConfig();
      context.setConfigured(true);
    }
  }

  private synchronized void authenticatorConfig() {
    // Does this Context require an Authenticator?
    SecurityConstraint constraints[] = context.findConstraints();
    if ((constraints == null) || (constraints.length == 0))
      return;
    LoginConfig loginConfig = context.getLoginConfig();
    if (loginConfig == null) {
      loginConfig = new LoginConfig("NONE", null, null, null);
      context.setLoginConfig(loginConfig);
    }

    // Has an authenticator been configured already?
    Pipeline pipeline = ((StandardContext) context).getPipeline();
    if (pipeline != null) {
      Valve basic = pipeline.getBasic();
      if ((basic != null) && (basic instanceof Authenticator))//一个context实例只能有一个验证器，
        return;                                               //所以当发现某个阀是验证器之后，就直接返回
      Valve valves[] = pipeline.getValves();
      for (int i = 0; i < valves.length; i++) {
        if (valves[i] instanceof Authenticator)
        return;
      }
    }
    else { // no Pipeline, cannot install authenticator valve
      return;
    }

    // Has a Realm been configured for us to authenticate against?
    if (context.getRealm() == null) {//检查context实例中是否有与之关联的领域对象，如果没有领域对象，就不需要安装
      return;                         //验证器了，因为用户无法通过身份验证的
    }

    // Identify the class name of the Valve we should configure
    String authenticatorName = "org.apache.catalina.authenticator.BasicAuthenticator";
    // Instantiate and install an Authenticator of the requested class
    Valve authenticator = null;
    try {
      Class authenticatorClass = Class.forName(authenticatorName);
      authenticator = (Valve) authenticatorClass.newInstance();
      ((StandardContext) context).addValve(authenticator);
      System.out.println("Added authenticator valve to Context");
    }
    catch (Throwable t) {
    }
  }
}