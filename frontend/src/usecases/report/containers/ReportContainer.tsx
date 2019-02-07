import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {paperStyle} from '../../../app/themes';
import {OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {Row, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Tab} from '../../../components/tabs/components/Tab';
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
import {TabName} from '../../../state/ui/tabs/tabsModels';
import {ReportIndicatorProps} from '../components/indicators/ReportIndicatorWidget';
import {ReportIndicatorWidgets, SelectedIndicatorWidgetProps} from '../components/indicators/ReportIndicatorWidgets';
import {QuantityDropdown} from '../components/QuantityDropdown';
import {reportIndicators} from '../reportModels';
import {MeasurementContentContainer} from './MeasurementContentContainer';

interface StateToProps extends SelectedIndicatorWidgetProps {
  enabledIndicatorTypes: Set<Medium>;
  selectedQuantities: Quantity[];
}

interface DispatchToProps {
  selectQuantities: (quantities: Quantity[]) => void;
  toggleReportIndicatorWidget: OnSelectIndicator;
}

type Props = StateToProps & DispatchToProps;

const noop = () => null;

const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 16, paddingTop: 0};

const ReportComponent = ({
  enabledIndicatorTypes,
  selectedIndicators,
  selectedQuantities,
  selectQuantities,
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
            <TabHeaders selectedTab={TabName.values} onChangeTab={noop}>
              <Tab tab={TabName.values} title={translate('measurements')}/>
            </TabHeaders>
            <RowRight className="Tabs-DropdownMenus">
              <QuantityDropdown
                selectedIndicators={selectedIndicators}
                selectedQuantities={selectedQuantities}
                onSelectQuantities={selectQuantities}
              />
            </RowRight>
          </TabTopBar>
          <MeasurementContentContainer/>
        </Tabs>
      </Paper>
    </PageLayout>
  );
};

const mapStateToProps =
  (rootState: RootState): StateToProps => {
    const {
      report: {selectedListItems},
      selectionTree: {entities},
      ui: {
        indicator: {
          selectedIndicators: {report},
          selectedQuantities,
        },
      },
    }: RootState = rootState;
    return ({
      enabledIndicatorTypes: getMedia({selectedListItems, entities}),
      selectedQuantities,
      selectedIndicators: report,
    });
  };

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectQuantities,
  toggleReportIndicatorWidget,
}, dispatch);

export const ReportContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportComponent);
