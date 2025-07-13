/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ua.vhlab.tnfvvc.data.iscsitargets;

import java.util.List;
import java.util.Map;

public class Dataset {

    public String id;
    public String type;
    public String name;
    public String pool;
    public boolean encrypted;
    public String encryption_root;
    public boolean key_loaded;
    public List<Dataset> children;

    public PropertyValue mountpoint;
    public PropertyValue deduplication;
    public PropertyValue aclmode;
    public PropertyValue acltype;
    public PropertyValue xattr;
    public PropertyValue atime;
    public PropertyValue casesensitivity;
    public PropertyValue checksum;
    public PropertyValue exec;
    public PropertyValue sync;
    public PropertyValue compression;
    public PropertyValue compressratio;
    public PropertyValue origin;
    public PropertyValue quota;
    public PropertyValue refquota;
    public PropertyValue reservation;
    public PropertyValue refreservation;
    public PropertyValue copies;
    public PropertyValue snapdir;
    public PropertyValue readonly;
    public PropertyValue recordsize;
    public PropertyValue key_format;
    public PropertyValue encryption_algorithm;
    public PropertyValue used;
    public PropertyValue usedbychildren;
    public PropertyValue usedbydataset;
    public PropertyValue usedbyrefreservation;
    public PropertyValue usedbysnapshots;
    public PropertyValue available;
    public PropertyValue special_small_block_size;
    public PropertyValue pbkdf2iters;
    public PropertyValue creation;
    public PropertyValue snapdev;
    public Map<String, Object> user_properties;
    public boolean locked;

    // Only for VOLUME
    public PropertyValue volsize;
    public PropertyValue volblocksize;
    public PropertyValue comments;

    public List<Dataset> getChildren() {
        return children;
    }
}
