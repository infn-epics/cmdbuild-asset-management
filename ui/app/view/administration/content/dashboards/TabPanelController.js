Ext.define('CMDBuildUI.view.administration.content.dashboards.TabPanelController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.administration-content-dashboards-tabpanel',
    requires: ['CMDBuildUI.util.administration.helper.TabPanelHelper'],

    control: {
        '#': {
            beforerender: 'onBeforeRender',
            tabchange: 'onTabChage'
        },
        '#adddashboard': {
            click: 'onNewBtnClick'
        }
    },

    /**
     * @param {CMDBuildUI.view.administration.content.classes.TabPanel} view
     * @param {Object} eOpts
     */
    onBeforeRender: function (view, eOpts) {
        const vm = this.getViewModel();
        const tabPanelHelper = CMDBuildUI.util.administration.helper.TabPanelHelper;
        tabPanelHelper.addTab(
            view,
            'properties',
            CMDBuildUI.locales.Locales.administration.common.strings.properties,
            [
                {
                    xtype: 'administration-content-dashboards-propertiestab'
                }
            ],
            0,
            {
                disabled: '{disabledTabs.properties}',
                hidden: '{hideForm}'
            }
        );

        tabPanelHelper.addTab(
            view,
            'permissions',
            CMDBuildUI.locales.Locales.administration.groupandpermissions.texts.permissions,
            [
                {
                    xtype: 'administration-content-dashboards-permissionstab'
                }
            ],
            1,
            {
                disabled: '{disabledTabs.permissions}',
                hidden: '{hideForm}'
            }
        );
        vm.set('activeTab', vm.get('activeTabs.dashboards'));
    },

    /**
     * @param {CMDBuildUI.view.administration.content.classes.TabPanel} view
     * @param {Ext.Component} newtab
     * @param {Ext.Component} oldtab
     * @param {Object} eOpts
     */
    onTabChage: function (view, newtab, oldtab, eOpts) {
        CMDBuildUI.util.administration.helper.TabPanelHelper.onTabChage(
            'activeTabs.dashboards',
            this,
            view,
            newtab,
            oldtab,
            eOpts
        );
    },

    /**
     *
     * @param {Ext.menu.Item} item
     * @param {Ext.event.Event} event
     * @param {Object} eOpts
     */
    onNewBtnClick: function (item, event, eOpts) {
        this.redirectTo('administration/dashboards/_new');
    }
});
