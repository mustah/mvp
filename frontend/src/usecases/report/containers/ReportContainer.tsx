import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {ResolutionProps, ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {withContent} from '../../../components/hoc/withContent';
import {OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {Row, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Tab} from '../../../components/tabs/components/Tab';
import {TabContent} from '../../../components/tabs/components/TabContent';
import {TabHeaders} from '../../../components/tabs/components/TabHeaders';
import {Tabs} from '../../../components/tabs/components/Tabs';
import {TabTopBar} from '../../../components/tabs/components/TabTopBar';
import {MainTitle} from '../../../components/texts/Titles';
import {PageLayout} from '../../../containers/PageLayout';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {SummaryContainer} from '../../../containers/SummaryContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {getMedia} from '../../../state/selection-tree/selectionTreeSelectors';
import {Medium, Quantity} from '../../../state/ui/graph/measurement/measurementModels';
import {selectQuantities, toggleReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {changeTabReport} from '../../../state/ui/tabs/tabsActions';
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {getSelectedTab} from '../../../state/ui/tabs/tabsSelectors';
import {OnSelectResolution} from '../../../state/user-selection/userSelectionModels';
import {CallbackWith, HasContent} from '../../../types/Types';
import {ReportIndicatorProps} from '../components/indicators/ReportIndicatorWidget';
import {ReportIndicatorWidgets, SelectedIndicatorWidgetProps} from '../components/indicators/ReportIndicatorWidgets';
import {QuantityDropdown} from '../components/QuantityDropdown';
import {selectResolution} from '../reportActions';
import {reportIndicators} from '../reportModels';
import {LegendContainer} from './LegendContainer';
import {GraphContainer, MeasurementsContainer} from './MeasurementsContainer';

interface StateToProps extends SelectedIndicatorWidgetProps {
  enabledIndicatorTypes: Set<Medium>;
  resolution: TemporalResolution;
  selectedQuantities: Quantity[];
  selectedTab: TabName;
}

interface DispatchToProps {
  changeTab: CallbackWith<TabName>;
  selectQuantities: (quantities: Quantity[]) => void;
  toggleReportIndicatorWidget: OnSelectIndicator;
  selectResolution: OnSelectResolution;
}

type Props = StateToProps & DispatchToProps;

const ResolutionDropdown = withContent<ResolutionProps & HasContent>(ResolutionSelection);

const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 16, paddingTop: 0};

const ReportComponent = ({
  changeTab,
  enabledIndicatorTypes,
  resolution,
  selectedTab,
  selectedIndicators,
  selectedQuantities,
  selectQuantities,
  selectResolution,
  toggleReportIndicatorWidget,
}: Props) => {
  const indicators: ReportIndicatorProps[] = reportIndicators();

  return (
    <PageLayout>
      <Row className="space-between">
        <MainTitle>{translate('report')}</MainTitle>
        <Row>
          <SummaryContainer/>
          <PeriodContainer/>
        </Row>
      </Row>

      <RowSpaceBetween>
        <ReportIndicatorWidgets
          indicators={indicators}
          selectedIndicators={selectedIndicators}
          onClick={toggleReportIndicatorWidget}
          enabledIndicatorTypes={enabledIndicatorTypes}
        />
      </RowSpaceBetween>

      <Paper style={contentStyle}>
        <Tabs className="ReportTabs">
          <TabTopBar>
            <TabHeaders selectedTab={selectedTab} onChangeTab={changeTab}>
              <Tab tab={TabName.graph} title={translate('graph')}/>
              <Tab tab={TabName.values} title={translate('measurements')}/>
            </TabHeaders>
            <RowRight className="Tabs-DropdownMenus">
              <ResolutionDropdown
                resolution={resolution}
                selectResolution={selectResolution}
                hasContent={selectedIndicators.length > 0}
              />
              <QuantityDropdown
                selectedIndicators={selectedIndicators}
                selectedQuantities={selectedQuantities}
                onSelectQuantities={selectQuantities}
              />
            </RowRight>
          </TabTopBar>
          <TabContent tab={TabName.graph} selectedTab={selectedTab}>
            <GraphContainer/>
          </TabContent>
          <TabContent tab={TabName.values} selectedTab={selectedTab}>
            <MeasurementsContainer/>
          </TabContent>
        </Tabs>
        <LegendContainer/>
      </Paper>
    </PageLayout>
  );
};

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      report: {resolution, selectedListItems},
      selectionTree: {entities},
      ui: {
        indicator: {
          selectedIndicators: {report},
          selectedQuantities,
        },
        tabs,
      },
    }: RootState = rootState;
    return ({
      enabledIndicatorTypes: getMedia({selectedListItems, entities}),
      resolution,
      selectedQuantities,
      selectedIndicators: report,
      selectedTab: getSelectedTab(tabs.report),
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeTab: changeTabReport,
  selectQuantities,
  selectResolution,
  toggleReportIndicatorWidget,
}, dispatch);

export const ReportContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportComponent);
