package net.labhackercd.edemocracia.data.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import net.labhackercd.edemocracia.data.provider.EDMContract.Category;
import net.labhackercd.edemocracia.data.provider.EDMContract.Group;
import net.labhackercd.edemocracia.data.provider.EDMContract.Message;
import net.labhackercd.edemocracia.data.provider.EDMContract.Thread;

/**
 * Provider that stores {@link EDMContract} data. Data is usually inserted
 * by {@link SyncHelper}, and queried by various {@link Activity} instances.
 */
public class EDMProvider extends ContentProvider {
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static final int GROUPS = 100;
    private static final int GROUPS_ID = 101;

    private static final int THREAD_ID = 201;
    private static final int ROOT_THREAD_ID = 202;

    private static final int CATEGORY_ID = 301;
    private static final int ROOT_CATEGORY_ID = 302;

    private static final int THREAD_MESSAGES = 401;
    private static final int ROOT_THREAD_MESSAGES = 402;
    private static final int THREAD_MESSAGES_ID = 403;
    private static final int ROOT_THREAD_MESSAGES_ID = 404;
    private static final int THREAD_MESSAGES_UUID = 405;
    private static final int ROOT_THREAD_MESSAGES_UUID = 406;

    private static UriMatcher buildUriMatcher() {

        /**
        // content://net.labhackercd.edemocracia.group -> GROUP_CONTENT_TYPE
        // content://net.labhackercd.edemocracia.group/:groupId -> GROUP_CONTENT_ITEM_TYPE
        // content://net.labhackercd.edemocracia.group/:groupId/forum -> FORUM_CONTENT_TYPE (?)
        // content://net.labhackercd.edemocracia.group/:groupId/category/:categoryId -> CATEGORY_CONTENT_ITEM_TYPE
        // content://net.labhacekrcd.edemocracia.group/:groupId/(category/:categoryId/)*thread/:threadId -> THREAD_CONTENT_ITEM_TYPE
        // content://net.labhacekrcd.edemocracia.group/:groupId/(category/:categoryId/)?thread/:threadId/messages -> MESSAGE_CONTENT_TYPE
        // content://net.labhacekrcd.edemocracia.group/:groupId/(category/:categoryId/)?thread/:threadId/messages/:messageUUID -> MESSAGE_CONTENT_ITEM_TYPE
        // NOTE: messageUUID identifies messages because we want to be able to accept both LocalMessages and Messages seamlessly.
        // NOTE: We could even go ahead and have various types of Uris, some with long ids, some with UUIDs, both pointing to the same resource.
         */

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = EDMContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "groups", GROUPS);
        matcher.addURI(authority, "groups/#", GROUPS_ID);

        matcher.addURI(authority, "groups/#/threads/#", ROOT_THREAD_ID);

        matcher.addURI(authority, "groups/#/categories/#", ROOT_CATEGORY_ID);

        matcher.addURI(authority, "groups/#/categories/#/threads/#", ROOT_THREAD_ID);

        matcher.addURI(authority, "groups/#/threads/#/messages", ROOT_THREAD_MESSAGES);
        matcher.addURI(authority, "groups/#/threads/#/messages/#", ROOT_THREAD_MESSAGES_ID);
        matcher.addURI(authority, "groups/#/threads/#/messages/*", ROOT_THREAD_MESSAGES_UUID);
        matcher.addURI(authority, "groups/#/categories/#/threads/#/messages", THREAD_MESSAGES);
        matcher.addURI(authority, "groups/#/categories/#/threads/#/messages/#", THREAD_MESSAGES_ID);
        matcher.addURI(authority, "groups/#/categories/#/threads/#/messages/*", THREAD_MESSAGES_UUID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        // TODO Save the OpenHelper?
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case GROUPS:
                return Group.CONTENT_TYPE;
            case GROUPS_ID:
                return Group.CONTENT_ITEM_TYPE;
            case CATEGORY_ID:
            case ROOT_CATEGORY_ID:
                return Category.CONTENT_ITEM_TYPE;
            case THREAD_ID:
            case ROOT_THREAD_ID:
                return Thread.CONTENT_ITEM_TYPE;
            case THREAD_MESSAGES:
            case ROOT_THREAD_MESSAGES:
                return Message.CONTENT_TYPE;
            case THREAD_MESSAGES_ID:
            case ROOT_THREAD_MESSAGES_ID:
            case THREAD_MESSAGES_UUID:
            case ROOT_THREAD_MESSAGES_UUID:
                return Message.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
