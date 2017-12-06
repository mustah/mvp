import * as React from 'react';
import {translate} from '../../../services/translationService';
import {PieChartSelector} from '../../../components/pie-chart-selector/PieChartSelector2';
import {Row} from '../../../components/layouts/row/Row';
import {uuid} from '../../../types/Types';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {DomainModel} from '../../../state/domain-models/domainModels';
import {dataSummary} from '../components/validationOverviewHelper';
import {ParameterName, SelectionParameter} from '../../../state/search/selection/selectionModels';
import {connect} from 'react-redux';
import {
  getMeterEntities,
  getMetersStatusAlarm, getMetersStatusOk,
  getMetersStatusUnknown,
} from '../../../state/domain-models/meter/meterSelectors';
import {getResultDomainModels} from '../../../state/domain-models/domainModelsSelectors';
import {RootState} from '../../../reducers/rootReducer';
import {bindActionCreators} from 'redux';
import {addSelection} from '../../../state/search/selection/selectionActions';
import {suffix} from '../../../services/formatters';
import {RaisedTabOption} from '../../../components/tabs/components/TabOption';
import {Column} from '../../../components/layouts/column/Column';
import {ValidationOverviewHeader} from '../components/ValidationOverviewHeader';
import {TabOptions} from '../../../components/tabs/components/TabOptions';
import {TabModel, TabName} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab, getTabs} from '../../../state/ui/tabs/tabsSelectors';
import {changeTabOptionValidation} from '../../../state/ui/tabs/tabsActions';
import * as classNames from 'classnames';

// TODO: Perhaps move this to themes and make customizable.
const colors: [string[]] = [
  ['#E91E63', '#fce8cc', '#3F51B5', '#2196F3', '#009688'],
  ['#1E88E5', '#FDD835', '#D81B60', '#00897B'],
  ['#b7e000', '#f7be29', '#ed4200'],
];

interface StateToProps {
  metersLookup: DomainModel<Meter>;
  meters: uuid[];
  metersStatusOk: uuid[];
  metersStatusAlarm: uuid[];
  metersStatusUnknown: uuid[];
  tabs: TabModel;
  selectedTab: TabName;
}

interface DispatchToProps {
  addSelection: (searchParameters: SelectionParameter) => void;
  changeTabOption: (tab: TabName, option: string) => void;
}

const ValidationOverview = (props: StateToProps & DispatchToProps) => {
  const {
    addSelection,
    meters,
    metersStatusOk,
    metersStatusAlarm,
    metersStatusUnknown,
    metersLookup,
    tabs,
    selectedTab,
    changeTabOption,
  } = props;

  const selectStatus = (id: uuid) => addSelection({parameter: ParameterName.meterStatuses, id});
  const selectCity = (id: uuid) => addSelection({parameter: ParameterName.cities, id});
  const selectManufacturer = (id: uuid) => addSelection({parameter: ParameterName.manufacturers, id});
  const selectAlarm = (id: uuid) => addSelection({parameter: ParameterName.alarms, id});

  const counts = {
    all: meters.length,
    ok: metersStatusOk.length,
    alarm: metersStatusAlarm.length,
    unknown: metersStatusUnknown.length,
  };

  const {selectedOption} = tabs.overview;
  const headings = {
    all: [
      translate('no meters'),
      translate('showing all meters'),
    ],
    ok: [
      translate('no meters that are ok'),
      translate('showing all meters that are ok'),
    ],
    unknown: [
      translate('no meters that have warnings'),
      translate('showing all meters that have warnings'),
    ],
    alarm: [
      translate('no meters that have faults'),
      translate('showing all meters that have faults'),
    ],
  };

  const metersByStatus = (selectedOption: string) => {
    switch (selectedOption) {
      case 'ok':
        return metersStatusOk;
      case 'alarm':
        return metersStatusAlarm;
      case 'unknown':
        return metersStatusUnknown;
      default:
        return meters;
    }
  };
  // TODO: Make sure it's the correct list of meters, that goes in as "meters"
  const PieChartData = dataSummary(metersByStatus(selectedOption), metersLookup);

  const overviewTabOptions: any[] = [
    {id: 'all', label: translate('all')},
    {id: 'ok', label: translate('ok')},
    {id: 'unknown', label: translate('unknown')},
    {id: 'alarm', label: translate('alarms')},
  ].map((section) => {
    section.label = `${section.label}: ${suffix(counts[section.id])}`;
    return section;
  }).map((section) => (
    <RaisedTabOption
      className={classNames(section.id)}
      id={section.id}
      key={section.id}
      title={section.label}
    />));

  const count = counts[selectedOption];
  const header = count ? `${headings[selectedOption][1]}: ${count}` : headings[selectedOption][0];

  const overviewHeader = (
    <ValidationOverviewHeader header={header}>
      <TabOptions tab={TabName.overview} selectedTab={selectedTab} select={changeTabOption} tabs={tabs}>
        {overviewTabOptions}
      </TabOptions>
    </ValidationOverviewHeader>
  );

// TODO: handle case when there are zero meters.
  return (
    <Column>
      {overviewHeader}
      <Row>
        <PieChartSelector
          heading={translate('status')}
          data={PieChartData.status}
          colors={colors[0]}
          onClick={selectStatus}
          maxSlices={4}
        />
        <PieChartSelector
          heading={translate('flagged for action')}
          data={PieChartData.flagged}
          colors={colors[1]}
          maxSlices={4}
        />
        <PieChartSelector
          heading={translate('alarm', {count: Object.keys(PieChartData.alarm).length})}
          data={PieChartData.alarm}
          colors={colors[0]}
          onClick={selectAlarm}
          maxSlices={4}
        />
        <PieChartSelector
          heading={translate('cities')}
          data={PieChartData.city}
          colors={colors[1]}
          onClick={selectCity}
          maxSlices={4}
        />
        <PieChartSelector
          heading={translate('manufacturer')}
          data={PieChartData.manufacturer}
          colors={colors[0]}
          onClick={selectManufacturer}
          maxSlices={4}
        />
        <PieChartSelector
          heading={translate('medium')}
          data={PieChartData.medium}
          colors={colors[1]}
          maxSlices={4}
        />
      </Row>
    </Column>
  );
};

const mapStateToProps = ({ui, domainModels: {meters}}: RootState) => {
  return {
    tabs: getTabs(ui.tabs.validation),
    selectedTab: getSelectedTab(ui.tabs.validation),
    metersLookup: getMeterEntities(meters),
    meters: getResultDomainModels(meters),
    metersStatusOk: getMetersStatusOk(meters),
    metersStatusAlarm: getMetersStatusAlarm(meters),
    metersStatusUnknown: getMetersStatusUnknown(meters),
  };
};

const mapDispatchToProps = dispatch => bindActionCreators({
  changeTabOption: changeTabOptionValidation,
  addSelection,
}, dispatch);

export const ValidationOverviewContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ValidationOverview);
