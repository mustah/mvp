import {default as classNames} from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import ContentSave from 'material-ui/svg-icons/content/save';
import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import EditorShowChart from 'material-ui/svg-icons/editor/show-chart';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {bgHoverColor, colors, iconSizeMedium} from '../../../app/themes';
import {DateRange, Period} from '../../../components/dates/dateModels';
import {PeriodSelection} from '../../../components/dates/PeriodSelection';
import {ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {Row, RowMiddle, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {IconProps, PopoverMenu} from '../../../components/popover/PopoverMenu';
import {Maybe} from '../../../helpers/Maybe';
import {firstUpperTranslated} from '../../../services/translationService';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {Clickable, Selectable} from '../../../types/Types';
import {LegendContainer} from '../containers/LegendContainer';
import {Props} from '../containers/ToolbarContainer';
import './Toolbar.scss';
import FlatButtonProps = __MaterialUI.FlatButtonProps;
import IconButtonProps = __MaterialUI.IconButtonProps;
import origin = __MaterialUI.propTypes.origin;

const roundedIconStyle: React.CSSProperties = {
  padding: 0,
  marginLeft: 8,
  width: 44,
  height: 44,
  borderRadius: 44 / 2,
};

const labelStyle: React.CSSProperties = {
  fontWeight: 'bold',
  fontSize: 12
};

const ToolbarIconButton = ({
  children,
  disabled,
  iconStyle,
  isSelected,
  onClick,
  style,
  tooltip,
}: IconButtonProps & Selectable) => (
  <IconButton
    iconStyle={iconStyle}
    onClick={onClick}
    className={classNames('ToolbarIconButton', disabled ? 'disabled' : '', {isSelected})}
    tooltip={tooltip}
    tooltipPosition="bottom-center"
    style={{...roundedIconStyle, ...style}}
  >
    {children}
  </IconButton>
);

const ToolbarActionButton = (props: FlatButtonProps) => (
  <FlatButton
    className={classNames('ToolbarActionButton', props.disabled ? 'disabled' : '')}
    labelPosition="after"
    {...props}
    hoverColor={bgHoverColor}
    labelStyle={labelStyle}
  />
);

const LegendActionButton = ({onClick, disabled}: Clickable & IconProps) => (
  <ToolbarIconButton
    disabled={disabled}
    iconStyle={iconSizeMedium}
    onClick={onClick}
    style={{marginRight: 16}}
    tooltip={firstUpperTranslated('filter')}
  >
    <ContentFilterList color={disabled ? colors.borderColor : colors.lightBlack}/>
  </ToolbarIconButton>
);

const anchorOrigin: origin = {horizontal: 'left', vertical: 'top'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};

const renderPopoverContent = () => <LegendContainer/>;

export const Toolbar = ({
  changeToolbarView,
  hasLegendItems,
  hasMeasurements,
  resolution,
  selectResolution,
  toggleLegend,
  exportToExcel,
  isFetching,
  isExportingToExcel,
  view,
  setReportTimePeriod,
  timePeriod,
}: Props) => {
  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const excelExport = () => exportToExcel();
  const selectPeriod = (period: Period) => setReportTimePeriod({period});
  const setCustomDateRange = (customDateRange: DateRange) => setReportTimePeriod({
    period: Period.custom,
    customDateRange
  });

  const customDateRange = Maybe.maybe(timePeriod.customDateRange);

  const legendIconProps: IconProps = {disabled: !hasLegendItems};

  return (
    <RowSpaceBetween className="Toolbar">
      <Row>
        <RowMiddle className="Toolbar-ViewSettings">
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
        </RowMiddle>

        <RowMiddle>
          <ToolbarActionButton
            disabled={true}
            style={{minWidth: 44}}
            icon={<ContentSave color={colors.borderColor}/>}
          />
          <ToolbarIconButton
            iconStyle={iconSizeMedium}
            disabled={isFetching || isExportingToExcel || !hasMeasurements}
            onClick={excelExport}
            style={{marginLeft: 16}}
            tooltip={firstUpperTranslated('export to excel')}
          >
            <CloudDownload color={colors.lightBlack} hoverColor={colors.iconHover}/>
          </ToolbarIconButton>
        </RowMiddle>
      </Row>

      <RowRight className={classNames('Tabs-DropdownMenus')}>
        <PopoverMenu
          popoverClassName="Popover-Legend"
          IconComponent={LegendActionButton}
          iconProps={legendIconProps}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          renderPopoverContent={renderPopoverContent}
        />
        <ResolutionSelection disabled={!hasMeasurements} resolution={resolution} selectResolution={selectResolution}/>
        <PeriodSelection
          disabled={!hasLegendItems}
          customDateRange={customDateRange}
          period={timePeriod.period}
          selectPeriod={selectPeriod}
          setCustomDateRange={setCustomDateRange}
          style={{marginBottom: 0, marginLeft: 0}}
        />
      </RowRight>
    </RowSpaceBetween>
  );
};
