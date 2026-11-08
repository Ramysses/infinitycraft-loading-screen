package br.com.infinitycrafts.hubui;

/** One launch-scoped destination ID; no arbitrary hosts, tokens or persisted reconnect loops. */
public final class LobbyStartup {
 private boolean consumed;
 private int ticks;
 public boolean connectLegacy152(boolean menuReady,boolean worldPresent,boolean playerSession,String target){
  if(consumed)return false;
  if(worldPresent){consumed=true;return false;}
  boolean lobbyTarget=target==null||target.trim().isEmpty()||"lobby".equalsIgnoreCase(target.trim());
  if(!playerSession||!lobbyTarget||!menuReady)return false;
  if(++ticks<20)return false;
  consumed=true;return true;
 }
}
