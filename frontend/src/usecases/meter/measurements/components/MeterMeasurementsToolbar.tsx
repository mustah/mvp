import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {colors} from '../../../../app/colors';
import {iconSizeMedium, svgIconProps} from '../../../../app/themes';
import {ToolbarIconButton} from '../../../../components/buttons/ToolbarIconButton';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {PeriodSelection} from '../../../../components/dates/PeriodSelection';
import {ResolutionSelection} from '../../../../components/dates/ResolutionSelection';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {Toolbar, ToolbarLeftPane, ToolbarRightPane, ToolbarViewSettings} from '../../../../components/toolbar/Toolbar';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../services/translationService';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {Props} from '../containers/MeterMeasurementsToolbarContainer';

export const MeterMeasurementsToolbar = ({
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
}: Props) => {
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
            <EditorFormatListBulleted color={colors.primaryFg}/>
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
            <CloudDownload  {...svgIconProps}/>
          </ToolbarIconButton>
        </RowMiddle>
      </ToolbarLeftPane>

      <ToolbarRightPane>
        <ResolutionSelection resolution={resolution} selectResolution={selectResolution}/>
        <PeriodSelection
          customDateRange={Maybe.maybe(timePeriod.customDateRange)}
          period={timePeriod.period}
          selectPeriod={selectPeriod}
          setCustomDateRange={setCustomDateRange}
          style={{marginBottom: 0, marginLeft: 0}}
        />
      </ToolbarRightPane>
    </Toolbar>
  );
};
