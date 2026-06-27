Ext.define('CMDBuildUI.view.administration.content.pluginmanager.Mixin', {
    mixinId: 'pluginmanager-mixin',
    mixins: ['CMDBuildUI.mixins.System'],

    /**
     *
     * @param {Ext.button.Button} button
     * @param {Event} event
     * @param {Object} eOpts
     */
    onReloadBtnClick: function (button, event, eOpts) {
        this.reloadApp();
    }
});
