import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import EditorShowChart from 'material-ui/svg-icons/editor/show-chart';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import Toggle from 'material-ui/Toggle';
import * as React from 'react';
import {colors, iconSizeMedium, svgIconProps} from '../../../app/themes';
import {ToolbarIconButton} from '../../../components/buttons/ToolbarIconButton';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {PeriodSelection} from '../../../components/dates/PeriodSelection';
import {ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {Row, RowMiddle} from '../../../components/layouts/row/Row';
import {IconProps} from '../../../components/popover/PopoverMenu';
import {Toolbar, ToolbarLeftPane, ToolbarRightPane, ToolbarViewSettings} from '../../../components/toolbar/Toolbar';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {Clickable} from '../../../types/Types';
import {Props} from '../containers/ToolbarContainer';

const LegendActionButton = ({onClick, disabled}: Clickable & IconProps) => (
  <ToolbarIconButton
    disabled={disabled}
    iconStyle={iconSizeMedium}
    onClick={onClick}
    tooltip={firstUpperTranslated('filter')}
  >
    <ContentFilterList color={disabled ? colors.borderColor : colors.lightBlack}/>
  </ToolbarIconButton>
);

export const ReportToolbar = ({
  canShowAverage,
  changeToolbarView,
  hasLegendItems,
  hasMeasurements,
  resolution,
  selectResolution,
  showHideLegend,
  exportToExcel,
  isFetching,
  isExportingToExcel,
  view,
  setReportTimePeriod,
  shouldComparePeriod,
  shouldShowAverage,
  timePeriod,
  toggleComparePeriod,
  toggleShowAverage,
}: Props) => {
  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const selectPeriod = (period: Period) => setReportTimePeriod({period});
  const setCustomDateRange = (customDateRange: DateRange) => setReportTimePeriod({
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
            <EditorShowChart color={colors.lightBlack} style={iconSizeMedium}/>
          </ToolbarIconButton>
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            isSelected={view === ToolbarView.table}
            onClick={selectTable}
            tooltip={firstUpperTranslated('table')}
          >
            <EditorFormatListBulleted color={colors.lightBlack}/>
          </ToolbarIconButton>
        </ToolbarViewSettings>

        <RowMiddle>
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            disabled={isFetching || isExportingToExcel || !hasMeasurements}
            onClick={exportToExcel}
            style={{marginLeft: 16}}
            tooltip={firstUpperTranslated('export to excel')}
          >
            <CloudDownload {...svgIconProps}/>
          </ToolbarIconButton>
        </RowMiddle>
      </ToolbarLeftPane>

      <ToolbarRightPane>
        <Row>
          <Toggle
            disabled={!canShowAverage}
            label={firstUpperTranslated('average')}
            defaultToggled={shouldShowAverage}
            onToggle={toggleShowAverage}
            style={{maxWidth: 200, marginRight: 16}}
          />
        </Row>
        <Row>
          <Toggle
            disabled={!hasLegendItems}
            label={firstUpperTranslated('compare period')}
            defaultToggled={shouldComparePeriod}
            onToggle={toggleComparePeriod}
            style={{maxWidth: 200, marginRight: 4}}
          />
        </Row>
        <ResolutionSelection disabled={!hasLegendItems} resolution={resolution} selectResolution={selectResolution}/>
        <PeriodSelection
          disabled={!hasLegendItems}
          customDateRange={customDateRange}
          period={timePeriod.period}
          selectPeriod={selectPeriod}
          setCustomDateRange={setCustomDateRange}
          style={{marginBottom: 0, marginLeft: 0}}
        />
        <LegendActionButton onClick={showHideLegend} disabled={!hasLegendItems}/>
      </ToolbarRightPane>
    </Toolbar>
  );
};
