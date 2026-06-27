Ext.define('CMDBuildUI.view.administration.content.localizations.localization.tabitems.CommonGridController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.administration-content-localizations-localization-tabitems-commongrid',

    control: {
        '#': {
            beforerender: 'onBeforeRender'
        }
    },

    onBeforeRender: function (view) {
        this.getViewModel().set('section', view.getSection());
        Ext.asap(function () {
            CMDBuildUI.util.Utilities.showLoader(true, view);
        });
    },

    onCancelBtnClick: function (button, e, eOpts) {
        var grid = this.getView();
        var vm = this.getViewModel();
        vm.set('actions.view', true);
        this.getView()
            .getColumns()
            .forEach(function (column) {
                if (!column.locked) {
                    column.setEditor(false);
                }
            });
        vm.set('actions.view', true);
        vm.set('actions.edit', false);
        grid.getStore().reload();
        vm.getParent().toggleEnableTabs();
    },
    onSaveBtnClick: function (button, e, eOpts) {
        var grid = this.getView();
        var vm = this.getViewModel();
        vm.set('actions.view', true);
        vm.set('actions.edit', false);
        vm.getParent().toggleEnableTabs();
        grid.getColumns().forEach(function (column) {
            if (!column.locked) {
                column.setEditor(false);
            }
        });

        var modifiedRecords = vm.get('localizationsStore').getModifiedRecords();
        var requestsCount = 0;
        modifiedRecords.forEach(function (record) {
            var data = {};
            var languges = record.get('values');
            Ext.Object.each(languges, function (key, value, myself) {
                data[key] = value;
            });
            var code = record.get('code');
            requestsCount++;
            Ext.Ajax.request({
                url: Ext.String.format('{0}/translations/{1}', CMDBuildUI.util.Config.baseUrl, code),
                method: 'PUT',
                jsonData: data,
                callback: function () {
                    requestsCount--;
                    if (requestsCount === 0) {
                        grid.getStore().reload();
                    }
                }
            });
        });
    },

    onstoreLoaded: function (store, records) {
        const grid = this.getView();
        const vm = this.getViewModel();

        const languagesStore = vm.get('languages');
        if (languagesStore.isLoaded()) {
            const languageRecords = languagesStore.getRange();
            const columns = [
                {
                    text: CMDBuildUI.locales.Locales.administration.localizations.element,
                    dataIndex: 'element',
                    align: 'left',
                    locked: true,
                    width: 200
                },
                {
                    text: CMDBuildUI.locales.Locales.administration.common.labels.type,
                    dataIndex: 'type',
                    align: 'left',
                    locked: true,
                    width: 200
                },
                {
                    text: CMDBuildUI.locales.Locales.administration.common.labels.code,
                    dataIndex: 'code',
                    align: 'left',
                    locked: true,
                    hidden: true,
                    width: 200
                },
                {
                    text: CMDBuildUI.locales.Locales.administration.localizations.defaulttranslation,
                    dataIndex: 'default',
                    align: 'left',
                    locked: true,
                    width: 200
                }
            ];

            languageRecords.forEach(function (record) {
                const lang = record.get('description');
                const code = record.get('code');
                const flag =
                    '<img width="20px" style="vertical-align:middle;margin-right:5px" src="resources/images/flags/' +
                    code +
                    '.png" />';

                const column = {
                    text: flag + lang,
                    dataIndex: code,
                    align: 'left',
                    locked: false
                };

                if ((grid.getWidth() - 600) / languageRecords.length < 150) {
                    column.minWidth = 150;
                    column.maxWidth = 150;
                } else {
                    column.flex = 1;
                }

                columns.push(column);
            });
            grid.reconfigure(store, columns);
            CMDBuildUI.util.Utilities.showLoader(false, grid);
        }
    },

    editedCell: function (editor, context, eOpts) {
        var me = this;
        var field = context.field;
        var modvalue = context.value;
        var store = me.getViewModel().get('localizationsStore');
        var key = context.record.get('code');

        var res = store.findRecord('code', key);
        if (res && res.get('values')[field] !== modvalue) {
            res.crudState = 'U';
            res.dirty = true;
            res.get('values')[field] = modvalue;
        }
    }
});
