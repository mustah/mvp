import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {colors, iconSizeMedium, svgIconProps} from '../../../../app/themes';
import {ToolbarIconButton} from '../../../../components/buttons/ToolbarIconButton';
import {DateRange, Period} from '../../../../components/dates/dateModels';
import {PeriodSelection} from '../../../../components/dates/PeriodSelection';
import {Row, RowMiddle, RowRight, RowSpaceBetween} from '../../../../components/layouts/row/Row';
import '../../../../components/toolbar/Toolbar.scss';
import {Maybe} from '../../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../../services/translationService';
import {ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {Props} from '../containers/MeterMeasurementsToolbarContainer';

export const MeterMeasurementsToolbar = ({
  changeToolbarView,
  hasMeasurements,
  exportToExcel,
  isExportingToExcel,
  isFetching,
  view,
  setMeterDetailsTimePeriod,
  timePeriod,
}: Props) => {
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const selectPeriod = (period: Period) => setMeterDetailsTimePeriod({period});
  const setCustomDateRange = (customDateRange: DateRange) => setMeterDetailsTimePeriod({
    period: Period.custom,
    customDateRange
  });

  return (
    <RowSpaceBetween className="Toolbar">
      <Row>
        <RowMiddle className="Toolbar-ViewSettings">
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            isSelected={view === ToolbarView.table}
            onClick={selectTable}
            tooltip={firstUpperTranslated('table')}
          >
            <EditorFormatListBulleted color={colors.lightBlack}/>
          </ToolbarIconButton>
        </RowMiddle>

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
      </Row>

      <RowRight className="Toolbar-RightPane">
        <PeriodSelection
          customDateRange={Maybe.maybe(timePeriod.customDateRange)}
          period={timePeriod.period}
          selectPeriod={selectPeriod}
          setCustomDateRange={setCustomDateRange}
          style={{marginBottom: 0, marginLeft: 0}}
        />
      </RowRight>
    </RowSpaceBetween>
  );
};