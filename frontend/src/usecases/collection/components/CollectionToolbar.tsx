import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import EditorShowChart from 'material-ui/svg-icons/editor/show-chart';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {colors} from '../../../app/colors';
import {iconSizeMedium, svgIconProps} from '../../../app/themes';
import {ToolbarIconButton} from '../../../components/buttons/ToolbarIconButton';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {PeriodSelection} from '../../../components/dates/PeriodSelection';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {Toolbar, ToolbarLeftPane, ToolbarRightPane, ToolbarViewSettings} from '../../../components/toolbar/Toolbar';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {DispatchToProps, StateToProps} from '../containers/CollectionToolbarContainer';

type Props = StateToProps & DispatchToProps;

export const CollectionToolbar = ({
  changeToolbarView,
  hasCollectionStats,
  exportToExcel,
  isExportingToExcel,
  isFetching,
  view,
  setCollectionTimePeriod,
  timePeriod,
}: Props) => {
  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const selectPeriod = (period: Period) => setCollectionTimePeriod({period});
  const setCustomDateRange = (customDateRange: DateRange) => setCollectionTimePeriod({
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
            <EditorShowChart color={colors.primaryFg} style={iconSizeMedium}/>
          </ToolbarIconButton>
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
            disabled={isFetching || isExportingToExcel || !hasCollectionStats}
            onClick={exportToExcel}
            style={{marginLeft: 16}}
            tooltip={firstUpperTranslated('export to excel')}
          >
            <CloudDownload {...svgIconProps}/>
          </ToolbarIconButton>
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
};
