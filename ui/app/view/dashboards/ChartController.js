Ext.define('CMDBuildUI.view.dashboards.ChartController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.dashboards-chart',

    control: {
        '#': { afterrender: 'onBeforeRender', collapse: 'onCollapse', expand: 'onExpand' },
        '#openInPopupBtn': { click: 'onOpenInPopupBtnClick' },
        '#showHideParamsBtn': { click: 'onShowHideParamsBtnClick' },
        '#showHideTableBtn': { click: 'onShowHideTableBtnClick' },
        '#downloadDataBtn': { click: 'onDownloadDataBtnClick' },
        '#refreshBtn': { click: 'onRefreshBtnClick' },
        '#downloadBtn': { click: 'onDownloadBtnClick' }
    },

    /**
     *
     * @param {CMDBuildUI.view.dashboards.Chart} view
     * @param {Object} eOpts
     */
    onBeforeRender: function (view, eOpts) {
        const me = this;
        const vm = view.lookupViewModel();
        const chart = vm.get('chart');

        if (chart.get('dataSourceType') === CMDBuildUI.model.dashboards.Chart.dataSourceTypes.funktion) {
            // add parameters
            const parameters = chart.dataSourceParameters();
            if (parameters.count()) {
                view.add(me.getParametersForm(parameters));
                vm.set('showhideparamsbtn.hidden', false);
            }
        }
        if (chart.get('type') !== CMDBuildUI.model.dashboards.Chart.charttypes.text) {
            chart.getSourceAttributes().then(function (attributes) {
                // set translations for axis value fields
                attributes.getRange().forEach(function (attribute) {
                    chart.set(
                        Ext.String.format('_{0}_translation', attribute.get('name')),
                        attribute.get('_description_translation')
                    );
                });
                me.initChart(view, chart);
            });
        } else {
            me.initChart(view, chart);
        }
    },

    /**
     *
     * @param {CMDBuildUI.view.dashboards.Chart} view
     * @param {Object} eOpts
     */
    onCollapse: function (view, eOpts) {
        view.lookupViewModel().set('toolsdisabled', true);
    },

    /**
     *
     * @param {CMDBuildUI.view.dashboards.Chart} view
     * @param {Object} eOpts
     */
    onExpand: function (view, eOpts) {
        view.lookupViewModel().set('toolsdisabled', false);
    },

    /**
     *
     * @param {Ext.button.Button} button
     * @param {Ext.event.Event} e
     */
    onLoadBtnClick: function (button, e) {
        button.lookupViewModel().get('records').load();
    },

    /**
     *
     * @param {Ext.button.Button} btn
     * @param {Object} eOpts
     */
    onOpenInPopupBtnClick: function (btn, eOpts) {
        const vm = this.getView().getViewModel();
        const clone = {
            xtype: 'dashboards-chart',
            showInPopup: true,
            scrollable: true,
            collapsible: true,
            viewModel: { data: { chart: vm.get('chart') } }
        };
        const popup = CMDBuildUI.util.Utilities.openPopup(null, '', clone);
        const chartVm = popup.down('dashboards-chart').getViewModel();
        chartVm.set('parameters', vm.get('parameters'));

        const recordbind = chartVm.bind('{records}', function (records) {
            if (records) {
                records.load();
                recordbind.destroy();
            }
        });
    },

    /**
     *
     * @param {Ext.button.Button} btn
     * @param {Boolean} pressed
     * @param {Object} eOpts
     */
    onShowHideParamsBtnClick: function (btn, pressed, eOpts) {
        const vm = btn.lookupViewModel();
        const hidden = !vm.get('form.hidden');
        vm.set('form.hidden', hidden);
        hidden ? btn.removeCls('active') : btn.addCls('active');
    },

    /**
     *
     * @param {Ext.button.Button} btn
     * @param {Boolean} pressed
     * @param {Object} eOpts
     */
    onShowHideTableBtnClick: function (btn, pressed, eOpts) {
        const vm = btn.lookupViewModel();
        const hidden = !vm.get('grid.hidden');
        vm.set('grid.hidden', hidden);
        hidden ? btn.removeCls('active') : btn.addCls('active');
        this.getView().fireEventArgs('showhidetable', [!hidden]);
    },

    /**
     *
     * @param {Ext.button.Button} btn
     * @param {Boolean} pressed
     * @param {Object} eOpts
     */
    onDownloadDataBtnClick: function (btn, pressed, eOpts) {
        const store = this.getViewModel().get('records');
        const records =
            store.type == 'buffered'
                ? Object.values(store.data.map).flatMap(function (r) {
                      return r.value;
                  })
                : store.getRange();
        const jsonData = Ext.Array.map(records, function (record) {
            const raw = record.getData();
            const data = {};
            Object.keys(raw).forEach(function (key) {
                if (key !== 'id') {
                    data[key] = raw[key];
                }
            });
            return data;
        });
        CMDBuildUI.util.File.downloadFromJSON(jsonData, 'csv');
    },

    /**
     *
     * @param {Ext.button.Button} btn
     * @param {Object} eOpts
     */
    onRefreshBtnClick: function (btn, eOpts) {
        btn.lookupViewModel().get('records').load();
    },

    /**
     * @param {Ext.button.Button} btn
     * @param {Object} eOpts
     */
    onDownloadBtnClick: function (btn, eOpts) {
        const view = this.getView();
        const chart = view.down('draw');
        if (chart) {
            if (Ext.os.is.Desktop) {
                CMDBuildUI.util.File.downloadBase64(chart.getImage().data, view.lookupViewModel().get('title'));
            } else {
                chart.preview();
            }
        }
    },

    /**
     *
     * @param {Ext.data.Store} store
     * @param {Ext.data.operation.Read} operation
     * @param {Object} eOpts
     */
    onStoreBeforeLoad: function (store, operation, eOpts) {
        const view = this.getView();
        view._loader = CMDBuildUI.util.Utilities.addLoadMask(view, { useTargetEl: true });
    },

    /**
     *
     * @param {Ext.data.Store} store
     * @param {Ext.data.Model[]} records
     * @param {Boolean} successful
     * @param {Ext.data.operation.Read} operation
     * @param {Object} eOpts
     */
    onStoreLoad: function (store, records, successful, operation, eOpts) {
        const view = this.getView();
        const chart = view.down('chart');

        if (chart && chart.getXType() === 'cartesian' && chart.getFlipXY()) {
            // set the the min height based on number of records
            const chartHeight = view.getViewModel().get('chart').get('height');
            const chartMinHeight = records ? records.length * 20 + 100 + (!Ext.isEmpty(chart.getLegend()) ? 45 : 0) : 0;
            if (chartHeight < chartMinHeight) {
                chart.setHeight(chartMinHeight);
            }
        }

        CMDBuildUI.util.Utilities.removeLoadMask(view._loader);
    },

    privates: {
        /**
         *
         * @property {Numeric} chartHeight
         */
        chartHeight: 250,

        /**
         *
         * @property {Numeric} gridMaxHeight
         */
        gridMaxHeight: 200,

        /**
         * Serie tooltip format
         * @property {Numeric} chartHeight
         */
        tooltipFormat: '<strong>{0}:</strong> {1}',

        /**
         *
         * @param {CMDBuildUI.view.dashboards.Chart} view
         * @param {Ext.data.Model} chart
         */
        initChart: function (view, chart) {
            const me = this;
            const vm = view.lookupViewModel();
            const isGrid = chart.get('type') === CMDBuildUI.model.dashboards.Chart.charttypes.table;
            let conf;
            // add chart
            switch (chart.get('type')) {
                case CMDBuildUI.model.dashboards.Chart.charttypes.bar:
                    conf = me.getBarConfig(chart);
                    break;
                case CMDBuildUI.model.dashboards.Chart.charttypes.gauge:
                    conf = me.getGaugeConfig(chart);
                    break;
                case CMDBuildUI.model.dashboards.Chart.charttypes.line:
                    conf = me.getLineConfig(chart);
                    break;
                case CMDBuildUI.model.dashboards.Chart.charttypes.pie:
                    conf = me.getPieConfig(chart);
                    break;
                case CMDBuildUI.model.dashboards.Chart.charttypes.table:
                    break;
                case CMDBuildUI.model.dashboards.Chart.charttypes.text:
                    conf = me.getTextConfig(chart);
                    break;
            }
            if (conf) {
                view.add(conf);
            }

            if (chart.get('type') !== CMDBuildUI.model.dashboards.Chart.charttypes.text) {
                // add grid
                me.addGrid(chart, view, isGrid);
                vm.set('showhidetablebtn.hidden', isGrid);
                vm.set('downloaddatabtn.hidden', false);
                vm.set('refreshbtn.hidden', false);
            }
            if (isGrid) {
                vm.set('grid.hidden', false);
            }
            view.lookupViewModel().set('toolsdisabled', view.collapsed);
        },

        /**
         *
         * @param {CMDBuildUI.view.dashboards.Chart} chart
         */
        getChartHeight: function (chart) {
            if (this.getView().getShowInPopup()) {
                return (this.getView().up().getHeight() / 100) * 75;
            }
            return parseInt(chart.get('height')) || this.chartHeight;
        },

        /**
         *
         * @param {CMDBuild.model.dashboards.Chart} chart
         * @return {Ext.form.Panel}
         */
        getParametersForm: function (parameters) {
            const me = this;
            const vm = this.getViewModel();
            const values = {};
            const paramsnames = {};
            const form = {
                xtype: 'form',
                bodyPadding: '0 10',
                fieldDefaults: CMDBuildUI.util.helper.FormHelper.fieldDefaults,
                items: [],
                bind: { hidden: '{form.hidden}' },
                dockedItems: [
                    {
                        xtype: 'toolbar',
                        dock: 'right',
                        defaults: { minWidth: 100 },
                        items: [
                            { xtype: 'tbfill' },
                            {
                                xtype: 'button',
                                formBind: true,
                                ui: 'management-primary-outline-small',
                                text: CMDBuildUI.locales.Locales.common.actions.load,
                                handler: me.onLoadBtnClick
                            }
                        ]
                    }
                ]
            };
            parameters.getRange().forEach(function (parameter, index) {
                const field = me.getEditorForParameter(parameter, index);
                form.items.push(field);
                values[field.name] = parameter.get('defaultValue');
                paramsnames[field.name] = field.metadata.attributename;
            });
            vm.set('parameters', Ext.create('Ext.data.Model', values));
            vm.set('paramsnames', paramsnames);
            return form;
        },

        /**
         *
         * @param {Object} parameter
         * @param {Integer} index
         * @return {Ext.form.Field}
         */
        getEditorForParameter: function (parameter) {
            const type = parameter.get('type').toLowerCase();
            let editor;

            // base editor config
            const field = {
                name: CMDBuildUI.util.Utilities.stringRemoveSpecialCharacters(parameter.get('name')),
                cmdbuildtype: parameter.get('type'),
                attributeconf: { attributename: parameter.get('name') },
                mandatory: parameter.get('required'),
                writable: true,
                description: parameter.get('_description_translation') || parameter.get('description'),
                getDescription: function () {
                    return this.description;
                }
            };
            const editorconfig = { linkName: 'parameters', mode: CMDBuildUI.util.helper.FormHelper.formmodes.update };

            if (parameter.get('defaultValue')) {
                editorconfig.defaultValue = { value: parameter.get('defaultValue') };
            }

            // customize for string and integer types
            if (
                type === CMDBuildUI.util.helper.ModelHelper.cmdbuildtypes.integer ||
                type === CMDBuildUI.util.helper.ModelHelper.cmdbuildtypes.bigint
            ) {
                switch (parameter.get('fieldType').toLowerCase()) {
                    case 'card':
                        const target = parameter.get('classToUseForReferenceWidget');
                        field.cmdbuildtype = CMDBuildUI.util.helper.ModelHelper.cmdbuildtypes.foreignkey;
                        field.attributeconf.preselectIfUnique = true;
                        field.attributeconf.targetType = CMDBuildUI.util.helper.ModelHelper.getObjectTypeByName(target);
                        field.attributeconf.targetClass = target;
                        break;
                    case 'lookup':
                        field.attributeconf.preselectIfUnique = parameter.get('preselectIfUnique');
                        field.cmdbuildtype = CMDBuildUI.util.helper.ModelHelper.cmdbuildtypes.lookup;
                        field.attributeconf.lookupType = parameter.get('lookupType');
                        break;
                }
                if (!Ext.isEmpty(parameter.get('ecqlFilter'))) {
                    field.attributeconf.ecqlFilter = parameter.get('ecqlFilter');
                }
            } else if (type === CMDBuildUI.util.helper.ModelHelper.cmdbuildtypes.string) {
                switch (parameter.get('fieldType').toLowerCase()) {
                    case 'classes':
                        editor = CMDBuildUI.util.helper.FormHelper.getFormField(field, editorconfig);
                        Ext.merge(editor, {
                            xtype: 'combobox',
                            valueField: 'name',
                            displayField: '_description_translation',
                            autoLoadOnValue: false,
                            queryMode: 'local',
                            bind: { store: '{classes}' }
                        });
                        break;
                }
            }
            if (!editor) {
                editor = CMDBuildUI.util.helper.FormHelper.getFormField(field, editorconfig);
            }
            return editor;
        },

        /**
         *
         * @param {CMDBuild.model.dashboards.Chart} chart
         * @return {Ext.chart.CartesianChart}
         */
        getBarConfig: function (chart) {
            const me = this;
            const xfield = chart.get('categoryAxisField');
            const xlabel = chart.get('_categoryAxisLabel_translation') || chart.get('categoryAxisLabel');
            const yfields = chart.get('valueAxisFields') || [];
            const ylabel = chart.get('_valueAxisLabel_translation') || chart.get('valueAxisLabel');
            const flipxy = chart.get('chartOrientation') === 'horizontal';
            // generate series
            const series = [
                {
                    type: 'bar',
                    xField: xfield,
                    yField: yfields,
                    title: me.getYfieldsTranslations(chart, yfields),
                    stacked: true,
                    style: { opacity: 0.8, minGapWidth: 5 },
                    highlightCfg: { opacity: 1 },
                    tooltip: {
                        trackMouse: true,
                        alwaysOnTop: CMDBuildUI.util.Utilities._popupAlwaysOnTop + 1,
                        renderer: function (tooltip, record, item) {
                            let html;
                            if (yfields.length > 1) {
                                html = Ext.String.format(
                                    '<strong>{0} - {1}:</strong> {2}',
                                    record.get(xfield),
                                    chart.get(Ext.String.format('_{0}_translation', item.field)),
                                    record.get(item.field)
                                );
                            } else {
                                html = Ext.String.format(me.tooltipFormat, record.get(xfield), record.get(item.field));
                            }
                            CMDBuildUI.util.Logger.log(
                                Ext.String.format('_{0}_translation', xfield) +
                                    ':' +
                                    chart.get(Ext.String.format('_{0}_translation', xfield)) +
                                    ' ' +
                                    html,
                                CMDBuildUI.util.Logger.levels.debug
                            );
                            tooltip.setHtml(html);
                        }
                    }
                }
            ];
            // return cartesian configuration
            return {
                xtype: 'cartesian',
                legend: chart.get('legend'),
                height: !this.getView().getShowInPopup() ? me.getChartHeight(chart) : me.getChartHeight(chart) * 1.2,
                flipXY: flipxy,
                theme: cmdbuildConfig.manifest,
                insetPadding: { right: 15 },
                animation: { easing: 'easeOut', duration: 400 },
                bind: { store: '{records}' },
                axes: [
                    {
                        type: 'numeric',
                        position: flipxy ? 'bottom' : 'left',
                        fields: yfields,
                        title: ylabel,
                        grid: true
                    },
                    {
                        type: 'category',
                        position: flipxy ? 'left' : 'bottom',
                        fields: xfield,
                        title: xlabel,
                        grid: true
                    }
                ],
                series: series
            };
        },

        /**
         *
         * @param {CMDBuild.model.dashboards.Chart} chart
         * @return {Ext.chart.PolarChart}
         */
        getPieConfig: function (chart) {
            const me = this;
            const labelfield = chart.get('labelField');
            const valuefield = chart.get('singleSeriesField');
            let legend = false;

            // check for legend
            if (chart.get('legend')) {
                legend = { docked: 'bottom' };
            }
            // return polar configuration
            return {
                xtype: 'polar',
                theme: cmdbuildConfig.manifest, //'default-gradients',
                height: me.getChartHeight(chart),
                bind: { store: '{records}' },
                legend: legend,
                interactions: ['rotate'],
                insetPadding: 5,
                innerPadding: 5,
                series: [
                    {
                        type: 'pie',
                        angleField: valuefield,
                        label: { field: labelfield, display: 'none' },
                        highlight: true,
                        tooltip: {
                            trackMouse: true,
                            alwaysOnTop: CMDBuildUI.util.Utilities._popupAlwaysOnTop + 1,
                            renderer: function (tooltip, record, item) {
                                if (Ext.isEmpty(record.get('percentage'))) {
                                    let sum = 0;
                                    this.getStore()
                                        .getRange()
                                        .forEach(function (i) {
                                            sum += i.get(valuefield);
                                        });
                                    record.set(
                                        'percentage',
                                        Ext.Number.toFixed((100 / sum) * record.get(valuefield), 2)
                                    );
                                }
                                tooltip.setHtml(
                                    Ext.String.format(
                                        me.tooltipFormat,
                                        record.get(labelfield),
                                        Ext.String.format(
                                            '{0} - {1}%',
                                            record.get(valuefield),
                                            record.get('percentage')
                                        )
                                    )
                                );
                            }
                        }
                    }
                ]
            };
        },

        /**
         *
         * @param {CMDBuild.model.dashboards.Chart} chart
         * @return {Ext.chart.CartesianChart}
         */
        getLineConfig: function (chart) {
            const me = this;
            const series = [];
            const xfield = chart.get('categoryAxisField');
            const xlabel = chart.get('_categoryAxisLabel_translation') || chart.get('categoryAxisLabel');
            const yfields = chart.get('valueAxisFields') || [];
            const ylabel = chart.get('_valueAxisLabel_translation') || chart.get('valueAxisLabel');

            // generate series
            yfields.forEach(function (yfield) {
                series.push({
                    type: 'line',
                    xField: xfield,
                    yField: yfield,
                    title: chart.get(Ext.String.format('_{0}_translation', yfield)) || yfield, // yfieldsTranslations, // yfield,
                    style: { lineWidth: 1, opacity: 0.8 },
                    marker: { radius: 4, lineWidth: 1, opacity: 0.7 },
                    highlight: { radius: 7, fillStyle: 'black', strokeStyle: 'white' },
                    tooltip: {
                        trackMouse: true,
                        alwaysOnTop: CMDBuildUI.util.Utilities._popupAlwaysOnTop + 1,
                        renderer: function (tooltip, record, item) {
                            tooltip.setHtml(
                                Ext.String.format(me.tooltipFormat, record.get(xfield), record.get(item.field))
                            );
                        }
                    }
                });
            });
            // return chart configuration
            return {
                xtype: 'cartesian',
                reference: 'chart',
                height: me.getChartHeight(chart),
                theme: cmdbuildConfig.manifest,
                legend: chart.get('legend'),
                animation: { duration: 200 },
                bind: { store: '{records}' },
                series: series,
                insetPadding: { right: 25 },
                innerPadding: 10,
                axes: [
                    { type: 'numeric', position: 'left', grid: true, title: ylabel },
                    { type: 'category', position: 'bottom', grid: true, title: xlabel }
                ]
            };
        },

        /**
         *
         * @param {CMDBuild.model.dashboards.Chart} chart
         * @return {Ext.chart.PolarChart}
         */
        getGaugeConfig: function (chart) {
            let fgcolor = chart.get('fgcolor');
            let bgcolor = chart.get('bgcolor');
            if (Ext.isEmpty(fgcolor) || Ext.isEmpty(bgcolor)) {
                const theme = Ext.Factory.chartTheme(cmdbuildConfig.manifest);
                const theme_colors = theme.getColors();
                fgcolor = fgcolor || theme_colors[0];
                bgcolor = bgcolor || theme_colors[1];
            }

            return {
                xtype: 'polar',
                theme: cmdbuildConfig.manifest,
                height: this.getChartHeight(chart),
                legend: chart.get('legend'),
                insetPadding: 30,
                bind: { store: '{records}' },
                axes: {
                    type: 'numeric',
                    position: 'gauge',
                    majorTickSteps: chart.get('steps'),
                    minimum: chart.get('minimum'),
                    maximum: chart.get('maximum')
                },
                series: {
                    type: 'gauge',
                    donut: 50,
                    angleField: chart.get('singleSeriesField'),
                    totalAngle: Math.PI,
                    needleLength: 100,
                    colors: [fgcolor, bgcolor]
                }
            };
        },

        /**
         *
         * @param {CMDBuild.model.dashboards.Chart} chart
         * @return {Ext.panel.Panel}
         */
        getTextConfig: function (chart) {
            const conf = {
                bodyPadding: CMDBuildUI.util.helper.FormHelper.properties.padding,
                html: chart.get('text'),
                height: this.getChartHeight(chart),
                scrollable: true
            };
            if (chart.get('height')) {
                conf.maxHeight = chart.get('height');
            }
            return conf;
        },

        /**
         *
         * @param {CMDBuildUI.model.Dashboards.Chart} chart
         * @param {CMDBuildUI.view.dashboards.Chart} view
         * @param {Boolean} isGrid
         */
        addGrid: function (chart, view, isGrid) {
            const me = this;
            chart.getSourceAttributes().then(function (attributes) {
                const columns = [];
                if (!me.destroyed) {
                    attributes.getRange().forEach(function (attribute) {
                        if (attribute.get('name') === chart.get('categoryAxisField')) {
                            attribute.set(
                                '_description_translation',
                                chart.get('_categoryAxisLabel_translation') ||
                                    chart.get('categoryAxisLabel') ||
                                    attribute.get('description') ||
                                    attribute.get('name')
                            );
                        }
                        if (chart.get('valueAxisFields').indexOf(attribute.get('name')) > -1) {
                            attribute.set(
                                '_description_translation',
                                attribute.get('_description_translation') ||
                                    chart.get('_valueAxisLabel_translation') ||
                                    chart.get('valueAxisLabel') ||
                                    attribute.get('description') ||
                                    attribute.get('name')
                            );
                        }
                        const field = CMDBuildUI.util.helper.ModelHelper.getModelField(attribute.getData());
                        const column = field ? CMDBuildUI.util.helper.GridHelper.getColumn(field) : null;
                        if (column) {
                            if (attribute.get('name') === chart.get('labelField')) {
                                column.text =
                                    chart.get('_labelField_translation') || chart.get('labelField') || column.text;
                            }
                            column.dataIndex = column.attributename;
                            columns.push(column);
                        }
                    });
                    const config = {
                        xtype: 'grid',
                        columns: columns,
                        maxHeight: me.gridMaxHeight,
                        hidden: true,
                        viewConfig: { enableTextSelection: true },
                        bind: { store: '{records}', hidden: '{grid.hidden}' }
                    };
                    if (isGrid) {
                        config.maxHeight = me.getChartHeight(chart);
                        config.height = me.getChartHeight(chart);
                        config.hidden = false;
                        const sourcetype = chart.get('dataSourceType');
                        if (sourcetype !== CMDBuildUI.model.dashboards.Chart.dataSourceTypes.funktion) {
                            config.columns.push({
                                xtype: 'actioncolumn',
                                minWidth: 30,
                                maxWidth: 30,
                                hideable: false,
                                align: 'center',
                                iconCls: CMDBuildUI.util.helper.IconHelper.getIconId('external-link-alt', 'solid'),
                                tooltip: CMDBuildUI.locales.Locales.common.actions.open,
                                handler: function (grid, rowIndex, colIndex, item, event, record) {
                                    let path;
                                    switch (sourcetype) {
                                        case CMDBuildUI.model.dashboards.Chart.dataSourceTypes.klass:
                                            path = 'classes/{0}/cards/{1}';
                                            break;
                                        case CMDBuildUI.model.dashboards.Chart.dataSourceTypes.process:
                                            path = 'processes/{0}/instances/{1}';
                                            break;
                                        case CMDBuildUI.model.dashboards.Chart.dataSourceTypes.view:
                                            path = 'views/{0}/items/{1}';
                                            break;
                                    }
                                    path = Ext.String.format(path, chart.get('dataSourceName'), record.get('_id'));
                                    me.redirectTo(path);
                                }
                            });
                        }
                    }
                    view.add(config);
                }
            });
        },

        /**
         *
         * @param {CMDBuildUI.model.Dashboards.Chart} chart
         * @param {String[]} yfields
         */
        getYfieldsTranslations: function (chart, yfields) {
            const _yfields = [];
            Ext.Array.forEach(yfields, function (yfield) {
                const translation = chart.get(Ext.String.format('_{0}_translation', yfield)) || yfield;
                _yfields.push(translation);
            });
            return _yfields;
        }
    }
});
