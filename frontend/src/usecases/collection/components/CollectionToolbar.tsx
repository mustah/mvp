import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import EditorShowChart from 'material-ui/svg-icons/editor/show-chart';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {iconSizeMedium} from '../../../app/themes';
import {ToolbarIconButton} from '../../../components/buttons/ToolbarIconButton';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {PeriodSelection} from '../../../components/dates/PeriodSelection';
import {withContent} from '../../../components/hoc/withContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {SmallLoader} from '../../../components/loading/SmallLoader';
import {Toolbar, ToolbarLeftPane, ToolbarRightPane, ToolbarViewSettings} from '../../../components/toolbar/Toolbar';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {OnChangeToolbarView, ToolbarView, ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, HasContent} from '../../../types/Types';

export interface StateToProps extends ToolbarViewSettingsProps {
  canExportToExcel?: boolean;
  hasCollectionStats: boolean;
  isFetching: boolean;
  isExportingToExcel: boolean;
  timePeriod: SelectionInterval;
}

export interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  exportToExcel: Callback;
  setCollectionStatsTimePeriod: CallbackWith<SelectionInterval>;
}

type Props = StateToProps & DispatchToProps & ThemeContext;

type WrapperProps = Props & HasContent;

const ExportToExcelButton = withContent<WrapperProps>(({
  cssStyles: {primary},
  hasCollectionStats,
  exportToExcel,
  isExportingToExcel,
  isFetching,
}: Props) => (
  <SmallLoader isFetching={isExportingToExcel} loadingStyle={{marginLeft: 6, marginTop: 2}}>
    <ToolbarIconButton
      iconStyle={iconSizeMedium}
      disabled={isFetching || !hasCollectionStats}
      onClick={exportToExcel}
      style={{marginLeft: 0}}
      tooltip={firstUpperTranslated('export to excel')}
    >
      <CloudDownload color={primary.fg} hoverColor={primary.fgHover}/>
    </ToolbarIconButton>
  </SmallLoader>
));

export const CollectionToolbar = withCssStyles((props: Props) => {
  const {
    changeToolbarView,
    cssStyles: {primary},
    view,
    setCollectionStatsTimePeriod,
    timePeriod,
  } = props;
  const exportToExcelProps: WrapperProps = {...props, hasContent: !!props.canExportToExcel};

  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const selectPeriod = (period: Period) => setCollectionStatsTimePeriod({period});
  const setCustomDateRange = (customDateRange: DateRange) => setCollectionStatsTimePeriod({
    period: Period.custom,
    customDateRange
  });

  const customDateRange = Maybe.maybe(timePeriod.customDateRange);

  return (
    <Toolbar>
      <ToolbarLeftPane>
        <ToolbarViewSettings>
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            isSelected={view === ToolbarView.graph}
            onClick={selectGraph}
            tooltip={firstUpperTranslated('graph')}
          >
            <EditorShowChart color={primary.fg} style={iconSizeMedium}/>
          </ToolbarIconButton>
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            isSelected={view === ToolbarView.table}
            onClick={selectTable}
            tooltip={firstUpperTranslated('table')}
          >
            <EditorFormatListBulleted color={primary.fg}/>
          </ToolbarIconButton>
        </ToolbarViewSettings>

        <RowMiddle>
          <ExportToExcelButton {...exportToExcelProps}/>
        </RowMiddle>
      </ToolbarLeftPane>

      <ToolbarRightPane>
        <PeriodSelection
          customDateRange={customDateRange}
          period={timePeriod.period}
          selectPeriod={selectPeriod}
          setCustomDateRange={setCustomDateRange}
          style={{marginBottom: 0, marginLeft: 0}}
        />
      </ToolbarRightPane>
    </Toolbar>
  );
});
