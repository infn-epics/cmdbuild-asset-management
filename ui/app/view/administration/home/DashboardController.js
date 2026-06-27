Ext.define('CMDBuildUI.view.administration.home.DashboardController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.administration-home-dashboard',

    control: {
        '#': {
            beforerender: 'onBeforeRender'
        }
    },

    onBeforeRender: function () {
        const vm = this.getViewModel();
        const store = Ext.getStore('pluginmanager.Plugins');

        if (!store) return;

        const now = new Date();
        const locales = CMDBuildUI.locales.Locales.administration.home;
        let html = '';

        const pluginsExpiring = store
            .getRange()
            .map(function (rec) {
                const exp = rec.get('expirationDate');
                if (!exp) return null;

                const diff = Ext.Date.diff(now, exp, Ext.Date.DAY);

                if (diff <= 30) {
                    return { name: rec.get('name'), days: diff };
                }
                return null;
            })
            .filter(Boolean);

        if (!pluginsExpiring.length) return;

        if (pluginsExpiring.length === 1) {
            html = `
                <span>${
                    pluginsExpiring[0].days === 1
                        ? Ext.String.format(locales.pluginexpirationitem_tomorrow, pluginsExpiring[0].name)
                        : Ext.String.format(
                              locales.pluginexpirationitem,
                              pluginsExpiring[0].name,
                              pluginsExpiring[0].days
                          )
                }</span>
            `;
        } else {
            html = `
                <span>${Ext.String.format(locales.pluginexpirationcount, pluginsExpiring.length)}</span>
                <ul style="padding-bottom: 0; margin-bottom: 0;">
                    ${pluginsExpiring
                        .map(
                            p =>
                                `<li>${
                                    p.days === 1
                                        ? Ext.String.format(locales.pluginexpirationitem_tomorrow, p.name, p.days)
                                        : Ext.String.format(locales.pluginexpirationitem, p.name, p.days)
                                }</li>`
                        )
                        .join('')}
                </ul>
            `;
        }

        vm.set({
            isWarningHidden: Ext.isEmpty(html),
            warningHtml: html
        });
    }
});
