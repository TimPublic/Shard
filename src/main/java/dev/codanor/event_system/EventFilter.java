package dev.codanor.event_system;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


public class EventFilter {


    // <editor-fold desc="-+- CREATION -+-">

    public EventFilter(boolean whitelistActive, boolean blacklistActive) {
        _BLACKLIST = new HashSet<>();
        _WHITELIST = new HashSet<>();
        _CHECKERS = new ArrayList<>();

        _whitelistIsActive = whitelistActive;
        _blacklistIsActive = blacklistActive;
    }

    // </editor-fold>

    // <editor-fold desc="-+- PROPERTIES -+-">

    // <editor-fold desc="NON FINALS">

    private boolean _whitelistIsActive, _blacklistIsActive;

    // </editor-fold>
    // <editor-fold desc="FINALS">

    private final HashSet<Class<I_Event>> _BLACKLIST, _WHITELIST;
    private final ArrayList<Function<I_Event, Boolean>> _CHECKERS;

    // </editor-fold>

    // </editor-fold>

    // <editor-fold desc="-+- BLACKLIST MANAGEMENT -+-">

    public boolean addToBlacklist(Class<I_Event> subclass) {
        return _BLACKLIST.add(subclass);
    }
    public void addToBlacklist(Collection<Class<I_Event>> subclasses) {
        subclasses.forEach(this::addToBlacklist);
    }

    public boolean rmvFromBlacklist(Class<I_Event> subclass) {
        return _BLACKLIST.remove(subclass);
    }
    public void rmvFromBlacklist(Collection<Class<I_Event>> subclasses) {
        subclasses.forEach(this::rmvFromBlacklist);
    }

    public void clearBlacklist() {
        _BLACKLIST.clear();
    }

    public Set<Class<I_Event>> getBlacklist() {
        return new HashSet<>(_BLACKLIST);
    }

    public boolean containsBlacklist(Class<I_Event> subclass) {
        return _BLACKLIST.contains(subclass);
    }

    public void setBlacklistStatus(boolean status) {
        _blacklistIsActive = status;
    }
    public boolean isBlacklistActive() {
        return _blacklistIsActive;
    }

    // </editor-fold>
    // <editor-fold desc="-+- WHITELIST MANAGEMENT -+-">

    public boolean addToWhitelist(Class<I_Event> subclass) {
        return _WHITELIST.add(subclass);
    }
    public void addToWhitelist(Collection<Class<I_Event>> subclasses) {
        subclasses.forEach(this::addToWhitelist);
    }

    public boolean rmvFromWhitelist(Class<I_Event> subclass) {
        return _WHITELIST.remove(subclass);
    }
    public void rmvFromWhitelist(Collection<Class<I_Event>> subclasses) {
        subclasses.forEach(this::rmvFromWhitelist);
    }

    public void clearWhitelist() {
        _WHITELIST.clear();
    }

    public Set<Class<I_Event>> getWhitelist() {
        return new HashSet<>(_WHITELIST);
    }

    public boolean containsWhitelist(Class<I_Event> subclass) {
        return _WHITELIST.contains(subclass);
    }

    public void setWhitelistStatus(boolean status) {
        _whitelistIsActive = status;
    }
    public boolean isWhitelistActive() {
        return _whitelistIsActive;
    }

    // </editor-fold>
    // <editor-fold desc="-+- CHECKER MANAGEMENT -+-">

    public void addChecker(Function<I_Event, Boolean> callback) {
        _CHECKERS.add(callback);
    }
    public boolean rmvChecker(Function<I_Event, Boolean> callback) {
        return _CHECKERS.remove(callback);
    }

    public void clearCheckers() {
        _CHECKERS.clear();
    }

    public Collection<Function<I_Event, Boolean>> getCheckers() {
        return new ArrayList<>(_CHECKERS);
    }

    // </editor-fold>

    // <editor-fold desc="-+- FILTER LOGIC -+-">

    public boolean filter(I_Event event) {
        if (_blacklistIsActive && _BLACKLIST.contains(event)) return false;
        if (_whitelistIsActive && !_WHITELIST.contains(event)) return false;

        for (Function<I_Event, Boolean> checker : _CHECKERS) if (!checker.apply(event)) return false;

        return true;
    }

    // </editor-fold>


}