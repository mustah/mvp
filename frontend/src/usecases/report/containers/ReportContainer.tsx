import Paper from 'material-ui/Paper';
import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {InjectedAuthRouterProps} from 'redux-auth-wrapper/history4/redirect';
import {paperStyle} from '../../../app/themes';
import {OnSelectIndicator} from '../../../components/indicators/indicatorWidgetModels';
import {
  SelectableIndicatorWidgets,
  SelectedIndicatorWidgetProps,
} from '../../../components/indicators/SelectableIndicatorWidgets';
import {Row} from '../../../components/layouts/row/Row';
import {MainTitle} from '../../../components/texts/Titles';
import {MvpPageContainer} from '../../../containers/MvpPageContainer';
import {PeriodContainer} from '../../../containers/PeriodContainer';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {selectReportIndicatorWidget} from '../../../state/ui/indicator/indicatorActions';
import {getSelectedIndicatorTypeForReport} from '../../../state/ui/indicator/indicatorSelectors';
import {indicators} from '../reportModels';
import {GraphContainer} from './GraphContainer';

interface DispatchToProps {
  selectIndicatorWidget: OnSelectIndicator;
}

type Props = SelectedIndicatorWidgetProps & DispatchToProps & InjectedAuthRouterProps;

const contentStyle: React.CSSProperties = {...paperStyle, marginTop: 24};

const ReportComponent = ({selectedIndicatorType, selectIndicatorWidget}: Props) => (
  <MvpPageContainer>
    <Row className="space-between">
      <MainTitle>{translate('report')}</MainTitle>
      <Row>
        {/*<SummaryContainer/>*/}
        <PeriodContainer/>
      </Row>
    </Row>

    <SelectableIndicatorWidgets
      indicators={indicators}
      selectedIndicatorType={selectedIndicatorType}
      selectIndicatorWidget={selectIndicatorWidget}
    />

    <Paper style={contentStyle}>
      <GraphContainer/>
    </Paper>
  </MvpPageContainer>
);

const mapStateToProps = ({ui}: RootState): SelectedIndicatorWidgetProps => ({
  selectedIndicatorType: getSelectedIndicatorTypeForReport(ui),
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  selectIndicatorWidget: selectReportIndicatorWidget,
}, dispatch);

export const ReportContainer =
  connect<SelectedIndicatorWidgetProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(ReportComponent);
