Ext.define('CMDBuildUI.view.administration.content.pluginmanager.PluginController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.administration-content-pluginmanager-plugin',
    mixins: ['CMDBuildUI.view.administration.content.pluginmanager.Mixin'],

    control: {
        '#': {
            afterlayout: 'onAfterLayout',
            beforerender: 'onBeforeRender'
        },
        '#applyPatchesBtn': {
            click: 'onApplyPatchesBtnClick'
        },
        '#patchesBtn': {
            click: 'onPatchesBtnClick'
        },
        '#cancelBtn': {
            click: 'onCancelBtnClick'
        },
        '#saveBtn': {
            click: 'onSaveBtnClick'
        },
        '#reloadButton': {
            click: 'onReloadBtnClick'
        }
    },

    /**
     *
     * @param {CMDBuildUI.view.administration.content.pluginmanager.View} view
     * @param {Object} eOpts
     */
    onBeforeRender: function (view, eOpts) {
        const vm = this.getViewModel();
        const thePlugin = vm.get('thePlugin');
        const configs = thePlugin ? thePlugin.get('configs') : {};
        const custompage = thePlugin ? thePlugin.get('custompage') : null;
        const hasPatches = thePlugin ? thePlugin.get('_hasPatches') : false;
        const expirationDate = thePlugin
            ? Ext.util.Format.date(
                  thePlugin.get('expirationDate'),
                  CMDBuildUI.util.helper.UserPreferences.getDateFormat()
              )
            : null;

        vm.set('isPatchesAvailable', hasPatches);

        if (thePlugin) {
            const pluginInfo = Ext.String.format(
                '<b>{0}</b> ({1} - V: {2}) - <b>{3}</b>: {4} - <b>{5}</b>: {6}',
                thePlugin.get('description'),
                thePlugin.get('name'),
                thePlugin.get('version'),
                CMDBuildUI.locales.Locales.administration.plugin.fieldlabels.requiredcoreversion,
                thePlugin.get('requiredCoreVersion'),
                CMDBuildUI.locales.Locales.administration.plugin.fieldlabels.expiration,
                expirationDate
                    ? expirationDate
                    : CMDBuildUI.locales.Locales.administration.plugin.fieldlabels.noexpiration
            );
            vm.set('pluginInfo', pluginInfo);

            let text, cls;
            switch (thePlugin.get('status')) {
                case CMDBuildUI.model.pluginmanager.Plugins.statuses.ready:
                    cls = 'ready';
                    text = CMDBuildUI.locales.Locales.attachments.statuses.ready;
                    break;
                case CMDBuildUI.model.pluginmanager.Plugins.statuses.error:
                    cls = 'error';
                    text = CMDBuildUI.locales.Locales.attachments.statuses.error;
                    break;
            }
            const pluginStatus = Ext.String.format(
                '{0} {1}',
                Ext.String.format('<span class="{0}pluginTag">{1}</span>', Ext.baseCSSPrefix, thePlugin.get('tag')),
                Ext.String.format('<span class="{0}pluginTag {1}">{2}</span>', Ext.baseCSSPrefix, cls, text)
            );
            vm.set('pluginStatus', pluginStatus);
        }

        if (!hasPatches) {
            if (!Ext.Object.isEmpty(configs) && !Ext.isEmpty(configs._model.attributes)) {
                const leftColumns = [];
                const rightColumns = [];

                Ext.Array.forEach(configs._model.attributes, function (item, index, allitems) {
                    const column = {
                        xtype: 'displayfield',
                        fieldLabel: item.name,
                        hidden: true,
                        bind: {
                            hidden: '{actions.edit}',
                            value: '{pluginConfigs.' + item._id.replace(/\./g, '__DOT__') + '}'
                        }
                    };

                    if (item._description_translation) {
                        column.labelToolIconQtip = item._description_translation;
                        column.labelToolIconCls = CMDBuildUI.util.helper.IconHelper.getIconId(
                            'question-circle',
                            'solid'
                        );
                    }

                    const fieldColumn = Ext.clone(column);

                    if (item.password) {
                        column.value = '•••••';
                        delete column.bind.value;
                    }

                    fieldColumn.bind.hidden = '{actions.view}';
                    switch (item.type) {
                        case 'integer':
                            fieldColumn.allowDecimals = false;
                        case 'float':
                            fieldColumn.xtype = 'numberfield';
                            break;
                        case 'select':
                            fieldColumn.xtype = 'combo';
                            const comboStore = [];
                            Ext.Array.forEach(item.options, function (combo, ind, allcombos) {
                                comboStore.push({
                                    text: combo
                                });
                            });
                            fieldColumn.store = Ext.create('Ext.data.Store', {
                                fields: ['text'],
                                data: comboStore
                            });
                            fieldColumn.valueField = 'text';
                            break;
                        case 'text':
                            fieldColumn.xtype = 'textareafield';
                            break;
                        case 'string':
                        default:
                            fieldColumn.xtype = 'textfield';
                            if (item.password) {
                                fieldColumn.xtype = 'passwordfield';
                            }
                            break;
                    }

                    if (index % 2 == 0) {
                        leftColumns.push(column);
                        leftColumns.push(fieldColumn);
                    } else {
                        rightColumns.push(column);
                        rightColumns.push(fieldColumn);
                    }
                });

                view.add({
                    xtype: 'fieldset',
                    collapsible: true,
                    layout: 'hbox',
                    scrollable: true,
                    flex: 1,
                    ui: 'administration-formpagination',
                    title: CMDBuildUI.locales.Locales.administration.plugin.pluginconfig,
                    localized: {
                        title: 'CMDBuildUI.locales.Locales.administration.plugin.pluginconfig'
                    },
                    items: [
                        {
                            flex: 1,
                            padding: '0 20 0 0',
                            items: leftColumns
                        },
                        {
                            flex: 1,
                            padding: '0 20 0 0',
                            items: rightColumns
                        }
                    ]
                });
            }

            if (custompage) {
                vm.set('hideButtons', true);
                view.add({
                    xtype: 'fieldset',
                    collapsible: true,
                    scrollable: true,
                    layout: 'fit',
                    flex: 1,
                    ui: 'administration-formpagination',
                    style: {
                        'padding-bottom': 0
                    },
                    title: CMDBuildUI.locales.Locales.administration.plugin.pluginconfig,
                    localized: {
                        title: 'CMDBuildUI.locales.Locales.administration.plugin.pluginconfig'
                    },
                    items: [
                        {
                            xtype: custompage.alias.replace('widget.', '')
                        }
                    ]
                });
            }
        }
    },

    /**
     *
     * @param {Ext.form.Panel} panel
     * @param {Ext.layout.form.Panel} layout
     * @param {Object} eOpts
     */
    onAfterLayout: function (panel, layout, eOpts) {
        Ext.GlobalEvents.fireEventArgs('showadministrationcontentmask', [false]);
    },

    /**
     *
     * @param {Ext.button.Button} button
     * @param {Event} event
     * @param {Object} eOpts
     */
    onCancelBtnClick: function (button, event, eOpts) {
        const vm = this.getViewModel();
        vm.set('recalculateConfigs', true);
        vm.set('action', CMDBuildUI.util.administration.helper.FormHelper.formActions.view);
    },

    /**
     *
     * @param {Ext.button.Button} button
     * @param {Event} event
     * @param {Object} eOpts
     */
    onSaveBtnClick: function (button, event, eOpts) {
        const vm = this.getViewModel();
        const configData = [];
        const thePlugin = vm.get('thePlugin');

        Ext.Object.each(vm.get('pluginConfigs'), function (key, value, myself) {
            const correctKey = key.replaceAll('__DOT__', '.');
            configData.push({
                access: vm.get('configsAttributes')[correctKey].access,
                key: correctKey,
                value: value
            });
        });

        // Prevent sending empty data
        if (!Ext.Object.isEmpty(configData)) {
            Ext.Ajax.request({
                method: 'PUT',
                url: Ext.String.format(
                    '{0}/plugin/{1}/config/_MANY',
                    CMDBuildUI.util.Config.baseUrl,
                    thePlugin.get('service') || thePlugin.get('_id')
                ),
                jsonData: configData,
                success: function (response, eOpts) {
                    const store = Ext.getStore('pluginmanager.Plugins');
                    const record = store.getById(thePlugin.getId());
                    Ext.Array.forEach(configData, function (item, index, allitems) {
                        record.get('configs')[item.key] = item.value;
                    });
                    vm.set('action', CMDBuildUI.util.administration.helper.FormHelper.formActions.view);
                }
            });
        } else {
            vm.set('action', CMDBuildUI.util.administration.helper.FormHelper.formActions.view);
        }
    },

    /**
     *
     * @param {Ext.button.Button} button
     * @param {Event} event
     * @param {Object} eOpts
     */
    onApplyPatchesBtnClick: function (button, event, eOpts) {
        this.onOpenPatchesPopup(true);
    },

    /**
     *
     * @param {Ext.button.Button} button
     * @param {Event} event
     * @param {Object} eOpts
     */
    onPatchesBtnClick: function (button, event, eOpts) {
        this.onOpenPatchesPopup();
    },

    privates: {
        /**
         *
         * @param {Boolean} showOnlyApplyPatches
         */
        onOpenPatchesPopup: function (showOnlyApplyPatches) {
            const vm = this.getViewModel();
            const content = {
                xtype: 'panel',
                items: [
                    {
                        xtype: 'container',
                        layout: 'hbox',
                        items: [
                            {
                                xtype: 'tbfill'
                            },
                            {
                                xtype: 'checkbox',
                                itemId: 'showOnlyApplyPatchesCheckbox',
                                padding: '10 20',
                                boxLabel: CMDBuildUI.locales.Locales.administration.plugin.showonlyapplypatches,
                                localized: {
                                    boxLabel: 'CMDBuildUI.locales.Locales.administration.plugin.showonlyapplypatches'
                                },
                                checked: showOnlyApplyPatches,
                                listeners: {
                                    change: function (field, newValue, oldValue, eOpts) {
                                        const store = field.lookupViewModel().get('patchesStore');
                                        if (newValue) {
                                            store.filterBy(function (record) {
                                                return !record.get('date');
                                            });
                                        } else {
                                            store.clearFilter();
                                        }
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'grid',
                        ui: 'cmdbuildgrouping',
                        scrollable: true,
                        forceFit: true,
                        features: [
                            {
                                ftype: 'grouping',
                                collapsible: true,
                                enableGroupingMenu: false,
                                groupHeaderTpl: [
                                    '{name} ({children:this.childrenNumber})',
                                    {
                                        childrenNumber: function (children) {
                                            return children.length;
                                        }
                                    }
                                ]
                            }
                        ],
                        bind: {
                            store: '{patchesStore}'
                        },
                        columns: [
                            {
                                text: CMDBuildUI.locales.Locales.administration.common.labels.name,
                                localized: {
                                    text: 'CMDBuildUI.locales.Locales.administration.common.labels.name'
                                },
                                dataIndex: '_id'
                            },
                            {
                                text: CMDBuildUI.locales.Locales.administration.plugin.appliedon,
                                localized: {
                                    text: 'CMDBuildUI.locales.Locales.administration.plugin.appliedon'
                                },
                                dataIndex: 'date',
                                renderer: function (value) {
                                    return CMDBuildUI.util.helper.FieldsHelper.renderTimestampField(value);
                                }
                            }
                        ]
                    }
                ],
                dockedItems: {
                    xtype: 'toolbar',
                    itemId: 'bottomToolbar',
                    dock: 'bottom',
                    ui: 'footer',
                    items: [
                        {
                            xtype: 'tbfill'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancelBtn',
                            text: CMDBuildUI.locales.Locales.administration.common.actions.cancel,
                            localized: {
                                text: 'CMDBuildUI.locales.Locales.administration.common.actions.cancel'
                            },
                            ui: 'administration-secondary-action-small',
                            listeners: {
                                click: function (button, event, eOpts) {
                                    popup.close();
                                }
                            }
                        },
                        {
                            xtype: 'button',
                            itemId: 'applyPatches',
                            text: CMDBuildUI.locales.Locales.administration.plugin.applypatches,
                            localized: {
                                text: 'CMDBuildUI.locales.Locales.administration.plugin.applypatches'
                            },
                            ui: 'administration-action-small',
                            bind: {
                                disabled: '{!isPatchesAvailable}'
                            },
                            listeners: {
                                click: function (button, event, eOpts) {
                                    const pluginName = vm.get('thePlugin').get('name');
                                    const cancelBtn = button.up('#bottomToolbar').down('#cancelBtn');
                                    button.showSpinner = true;
                                    CMDBuildUI.util.Utilities.disableFormButtons([button, cancelBtn]);

                                    Ext.Ajax.request({
                                        method: 'POST',
                                        url: Ext.String.format(
                                            '{0}/system/plugins/{1}/patch',
                                            CMDBuildUI.util.Config.baseUrl,
                                            pluginName
                                        ),
                                        callback: function (options, success, response) {
                                            CMDBuildUI.util.administration.MenuStoreBuilder.initialize(function () {
                                                const plugins = Ext.getStore('pluginmanager.Plugins').getRange();
                                                const plugin = Ext.Array.findBy(plugins, function (item, index) {
                                                    return item.get('name') == pluginName;
                                                });
                                                const popupViewModel = button.lookupViewModel();
                                                const store = popupViewModel.get('patchesStore');

                                                if (success) {
                                                    CMDBuildUI.util.Notifier.showInfoMessage(
                                                        CMDBuildUI.locales.Locales.administration.plugin.patchesapplied
                                                    );
                                                    vm.set('thePlugin', plugin);
                                                    vm.set('isPatchesAvailable', plugin.get('_hasPatches'));
                                                    vm.set('recalculateConfigs', true);
                                                } else {
                                                    CMDBuildUI.util.Notifier.showErrorMessage(
                                                        CMDBuildUI.locales.Locales.administration.plugin
                                                            .patchesapplyfailed,
                                                        null,
                                                        'default',
                                                        CMDBuildUI.locales.Locales.administration.plugin
                                                            .patchesapplyfailedmessage
                                                    );
                                                }

                                                button
                                                    .up('#plugin-manager-patches')
                                                    .down('#showOnlyApplyPatchesCheckbox')
                                                    .setValue(false);
                                                popupViewModel.set('isPatchesAvailable', plugin.get('_hasPatches'));
                                                store.removeAll();
                                                store.add(plugin.get('patches'));

                                                CMDBuildUI.util.Utilities.enableFormButtons([button, cancelBtn]);
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    ]
                }
            };

            const patchesStore = {
                autoDestroy: true,
                proxy: {
                    type: 'memory'
                },
                data: vm.get('thePlugin').get('patches'),
                sorters: {
                    property: 'date',
                    direction: 'DESC'
                },
                grouper: {
                    groupFn: function (item) {
                        return item.get('date')
                            ? CMDBuildUI.locales.Locales.administration.plugin.applied
                            : CMDBuildUI.locales.Locales.administration.plugin.tobeapplied;
                    }
                }
            };

            if (showOnlyApplyPatches) {
                patchesStore.filters = function (record) {
                    return !record.get('date');
                };
            }

            const popup = CMDBuildUI.util.Utilities.openPopup(
                'plugin-manager-patches',
                CMDBuildUI.locales.Locales.administration.plugin.pluginpatcheslist,
                content,
                null,
                {
                    ui: 'administration',
                    width: '60%',
                    height: '60%',
                    viewModel: {
                        data: {
                            isPatchesAvailable: vm.get('isPatchesAvailable')
                        },
                        stores: {
                            patchesStore: patchesStore
                        }
                    }
                }
            );
        }
    }
});
