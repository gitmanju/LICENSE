package com.dheemai.treecounter.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TreeDatabase_Impl extends TreeDatabase {
  private volatile TreeDao _treeDao;

  private volatile FarmPlotDao _farmPlotDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `trees` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `species` TEXT NOT NULL, `notes` TEXT NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, `photoPath` TEXT, `timestamp` INTEGER NOT NULL, `additionalNames` TEXT NOT NULL, `plotName` TEXT NOT NULL, `plotId` INTEGER NOT NULL, `canvasX` REAL NOT NULL, `canvasY` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `plots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'cea65188c868052676230fe3d9ece302')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `trees`");
        db.execSQL("DROP TABLE IF EXISTS `plots`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTrees = new HashMap<String, TableInfo.Column>(12);
        _columnsTrees.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("species", new TableInfo.Column("species", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("latitude", new TableInfo.Column("latitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("longitude", new TableInfo.Column("longitude", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("photoPath", new TableInfo.Column("photoPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("additionalNames", new TableInfo.Column("additionalNames", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("plotName", new TableInfo.Column("plotName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("plotId", new TableInfo.Column("plotId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("canvasX", new TableInfo.Column("canvasX", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTrees.put("canvasY", new TableInfo.Column("canvasY", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTrees = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTrees = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTrees = new TableInfo("trees", _columnsTrees, _foreignKeysTrees, _indicesTrees);
        final TableInfo _existingTrees = TableInfo.read(db, "trees");
        if (!_infoTrees.equals(_existingTrees)) {
          return new RoomOpenHelper.ValidationResult(false, "trees(com.dheemai.treecounter.data.Tree).\n"
                  + " Expected:\n" + _infoTrees + "\n"
                  + " Found:\n" + _existingTrees);
        }
        final HashMap<String, TableInfo.Column> _columnsPlots = new HashMap<String, TableInfo.Column>(3);
        _columnsPlots.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlots.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlots.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlots = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlots = new TableInfo("plots", _columnsPlots, _foreignKeysPlots, _indicesPlots);
        final TableInfo _existingPlots = TableInfo.read(db, "plots");
        if (!_infoPlots.equals(_existingPlots)) {
          return new RoomOpenHelper.ValidationResult(false, "plots(com.dheemai.treecounter.data.FarmPlot).\n"
                  + " Expected:\n" + _infoPlots + "\n"
                  + " Found:\n" + _existingPlots);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "cea65188c868052676230fe3d9ece302", "da02a2e3eb697a0cfaf3ebca2bcbb02b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "trees","plots");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `trees`");
      _db.execSQL("DELETE FROM `plots`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TreeDao.class, TreeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FarmPlotDao.class, FarmPlotDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TreeDao treeDao() {
    if (_treeDao != null) {
      return _treeDao;
    } else {
      synchronized(this) {
        if(_treeDao == null) {
          _treeDao = new TreeDao_Impl(this);
        }
        return _treeDao;
      }
    }
  }

  @Override
  public FarmPlotDao farmPlotDao() {
    if (_farmPlotDao != null) {
      return _farmPlotDao;
    } else {
      synchronized(this) {
        if(_farmPlotDao == null) {
          _farmPlotDao = new FarmPlotDao_Impl(this);
        }
        return _farmPlotDao;
      }
    }
  }
}
