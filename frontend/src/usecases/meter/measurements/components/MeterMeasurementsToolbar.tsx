import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {iconSizeMedium} from '../../../../app/themes';
import {ToolbarIconButton} from '../../../../components/buttons/ToolbarIconButton';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {PeriodSelection} from '../../../../components/dates/PeriodSelection';
import {ResolutionSelection} from '../../../../components/dates/ResolutionSelection';
import {ThemeContext} from '../../../../components/hoc/withThemeProvider';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Toolbar, ToolbarLeftPane, ToolbarRightPane, ToolbarViewSettings} from '../../../../components/toolbar/Toolbar';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../services/translationService';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {Props} from '../containers/MeterMeasurementsToolbarContainer';

export const MeterMeasurementsToolbar = ({
  cssStyles: {primary},
  changeToolbarView,
  exportToExcel,
  hasMeasurements,
  isExportingToExcel,
  isFetching,
  resolution,
  selectResolution,
  setTimePeriod,
  timePeriod,
  view,
}: Props & ThemeContext) => {
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const selectPeriod = (period: Period) => setTimePeriod({period});
  const setCustomDateRange = (customDateRange: DateRange) => setTimePeriod({
    period: Period.custom,
    customDateRange
  });

  return (
    <Toolbar>
      <ToolbarLeftPane>
        <ToolbarViewSettings>
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
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            disabled={isFetching || isExportingToExcel || !hasMeasurements}
            onClick={exportToExcel}
            style={{marginLeft: 0}}
            tooltip={firstUpperTranslated('export to excel')}
          >
            <CloudDownload color={primary.fg} hoverColor={primary.fgHover}/>
          </ToolbarIconButton>
        </RowMiddle>
      </ToolbarLeftPane>

      <ToolbarRightPane>
        <PeriodSelection
          customDateRange={Maybe.maybe(timePeriod.customDateRange)}
          period={timePeriod.period}
          selectPeriod={selectPeriod}
          setCustomDateRange={setCustomDateRange}
          style={{marginBottom: 0, marginLeft: 0}}
        />
        <ResolutionSelection resolution={resolution} selectResolution={selectResolution}/>
      </ToolbarRightPane>
    </Toolbar>
  );
};
