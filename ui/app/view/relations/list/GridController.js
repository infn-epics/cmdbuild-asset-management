Ext.define('CMDBuildUI.view.relations.list.GridController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.relations-list-grid',

    control: {
        '#': {
            beforerender: 'onBeforeRender',
            itemdblclick: 'onItemDblClick'
        },
        'tableview': {
            actionopencard: 'onActionOpenCard',
            actioneditrelation: 'onActionEditRelation',
            actiondeleterelation: 'onActionDeleteRelation',
            actioneditcard: 'onActionEditCard'
        }
    },

    onBeforeRender: function (view) {
        //sets the height of the grid;
        view.calculateHeight();

        view.lookupViewModel().bind(
            {
                bindTo: '{allRelations}'
            },
            function (store) {
                var loadmask = CMDBuildUI.util.Utilities.addLoadMask(view);
                // enable remote sort here because
                // autoLoad=false is ignored when grouping is actived
                // and remoteSort is set to true. See EXTJS-19781.
                store.setRemoteSort(true);
                // load store
                store.load({
                    callback: function () {
                        CMDBuildUI.util.Utilities.removeLoadMask(loadmask);
                    },
                    scope: this
                });
            }
        );
    },

    /**
     * @param {CMDBuildUI.view.attachments.Grid} grid
     * @param {Ext.data.Model} record
     * @param {Number} rowIndex
     * @param {Number} colIndex
     * @param {Boolean} openInGrid
     *
     */
    onActionOpenCard: function (grid, record, rowIndex, colIndex, openInGrid) {
        const destinationType = record.get('_destinationType');
        const destinationId = record.get('_destinationId');
        let action = null;
        const destinationObj = CMDBuildUI.util.helper.ModelHelper.getObjectFromName(destinationType);

        // Need both for prevent the opening of the superclass on the view
        if (
            CMDBuildUI.util.helper.Configurations.get(CMDBuildUI.model.Configuration.ui.redirectTo.view) &&
            CMDBuildUI.util.helper.Configurations.get(CMDBuildUI.model.Configuration.ui.redirectTo.subclass)
        ) {
            action = CMDBuildUI.mixins.DetailsTabPanel.actions.view;
        } else if (!openInGrid) {
            // Configuration win over params
            action = CMDBuildUI.mixins.DetailsTabPanel.actions.view;
        }

        /**
         *
         * @param {String} objectName
         */
        const redirectTo = function (objectName) {
            let url = '';
            switch (CMDBuildUI.util.helper.ModelHelper.getObjectTypeByName(destinationType)) {
                case CMDBuildUI.util.helper.ModelHelper.objecttypes.klass:
                    url = CMDBuildUI.util.Navigation.getClassBaseUrl(objectName, destinationId, action, true);
                    break;
                case CMDBuildUI.util.helper.ModelHelper.objecttypes.process:
                    url = CMDBuildUI.util.Navigation.getProcessBaseUrl(objectName, destinationId, action, null, true);
                    break;
                default:
                    return;
            }
            CMDBuildUI.util.Utilities.closeAllPopups();
            CMDBuildUI.util.Utilities.redirectTo(url);
        };
        // is a superclass?
        if (
            destinationObj.get('prototype') &&
            CMDBuildUI.util.helper.Configurations.get(CMDBuildUI.model.Configuration.ui.redirectTo.subclass)
        ) {
            CMDBuildUI.util.api.Client.getRemoteCard(
                CMDBuildUI.util.helper.ModelHelper.systemClasses.class,
                destinationId
            ).then(function (card) {
                redirectTo(card.get('_type'));
            });
        } else {
            redirectTo(destinationType);
        }
    },

    /**
     * @param {CMDBuildUI.view.relations.list.Grid} grid
     * @param {Ext.data.Model} record
     * @param {Number} rowIndex
     * @param {Number} colIndex
     *
     */
    onActionEditRelation: function (grid, record, rowIndex, colIndex) {
        var vm = grid.lookupViewModel();
        CMDBuildUI.view.relations.Utils.editRelation(record, {
            theObject: vm.get('theObject'),
            proxyurl: vm.get('storedata.proxyurl'),
            objecttypename: vm.get('objectTypeName'),
            objectid: vm.get('objectId')
        }).then(function () {
            grid.getStore().reload({
                callback: function () {
                    Ext.GlobalEvents.fireEventArgs('updateMasterDetailStore', [record.get('_type')]);
                    Ext.GlobalEvents.fireEvent('reloadFieldsetGrid');
                }
            });
        });
    },

    /**
     * @param {CMDBuildUI.view.relations.list.Grid} grid
     * @param {Ext.data.Model} record
     * @param {Number} rowIndex
     * @param {Number} colIndex
     *
     */
    onActionDeleteRelation: function (grid, record, rowIndex, colIndex) {
        CMDBuildUI.view.relations.Utils.deleteRelation(record)
            .then(function () {
                Ext.GlobalEvents.fireEventArgs('updateMasterDetailStore', [record.get('_type')]);
                Ext.GlobalEvents.fireEvent('reloadFieldsetGrid');
            })
            .otherwise(function () {
                grid.getStore().reload();
            });
    },

    /**
     * @param {CMDBuildUI.view.relations.list.Grid} grid
     * @param {Ext.data.Model} record
     * @param {Number} rowIndex
     * @param {Number} colIndex
     *
     */
    onActionEditCard: function (grid, record, rowIndex, colIndex) {
        var popup,
            config = {
                xtype: 'classes-cards-card-edit',
                padding: 10,
                viewModel: {
                    data: {
                        objectTypeName: record.get('_destinationType'),
                        objectId: record.get('_destinationId')
                    }
                },
                hideInlineElements: false,
                buttons: [
                    {
                        ui: 'secondary-action',
                        itemId: 'detailclosebtn',
                        text: CMDBuildUI.locales.Locales.common.actions.close,
                        autoEl: {
                            'data-testid': 'relations-list-grid-editcard-cancel'
                        },
                        localized: {
                            text: 'CMDBuildUI.locales.Locales.common.actions.close'
                        },
                        handler: function (btn, event) {
                            popup.destroy();
                        }
                    },
                    {
                        ui: 'management-primary',
                        itemId: 'detailsavebtn',
                        text: CMDBuildUI.locales.Locales.common.actions.save,
                        autoEl: {
                            'data-testid': 'relations-list-grid-editcard-save'
                        },
                        formBind: true,
                        localized: {
                            text: 'CMDBuildUI.locales.Locales.common.actions.save'
                        },
                        handler: function (btn, event) {
                            CMDBuildUI.util.helper.FormHelper.startSavingForm();
                            var cancelBtn = this.up().down('#detailclosebtn');
                            btn.showSpinner = true;
                            CMDBuildUI.util.Utilities.disableFormButtons([btn, cancelBtn]);

                            popup
                                .down('classes-cards-card-edit')
                                .getController()
                                .saveForm({
                                    failure: function () {
                                        CMDBuildUI.util.helper.FormHelper.endSavingForm();
                                        CMDBuildUI.util.Utilities.enableFormButtons([btn, cancelBtn]);
                                    }
                                })
                                .then(function () {
                                    grid.getStore().load({
                                        callback: function () {
                                            Ext.GlobalEvents.fireEventArgs('updateMasterDetailStore', [
                                                record.get('_type')
                                            ]);
                                            Ext.GlobalEvents.fireEvent('reloadFieldsetGrid');
                                            CMDBuildUI.util.helper.FormHelper.endSavingForm();
                                            popup.destroy();
                                        }
                                    });
                                })
                                .otherwise(function () {
                                    CMDBuildUI.util.helper.FormHelper.endSavingForm();
                                    CMDBuildUI.util.Utilities.enableFormButtons([btn, cancelBtn]);
                                });
                        }
                    }
                ],

                listeners: {
                    itemupdated: function () {
                        popup.close();
                        grid.getStore().load();
                    },
                    cancelupdating: function () {
                        popup.close();
                    }
                }
            };

        // open popup
        popup = CMDBuildUI.util.Utilities.openPopup(null, record.get('_destinationDescription'), config);
    },

    /**
     * @param {CMDBuildUI.view.relations.list.Grid} grid
     * @param {Ext.data.Model} record
     * @param {HTMLElement} item
     * @param {Number} index
     * @param {Ext.event.Event} e
     * @param {Object} eOpts
     */
    onItemDblClick: function (grid, record, item, index, e, eOpts) {
        this.onActionOpenCard(grid, record, null, null, grid.lookupViewModel().get('readonly'));
    }
});
