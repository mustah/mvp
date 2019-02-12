import {default as classNames} from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import ContentSave from 'material-ui/svg-icons/content/save';
import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import EditorShowChart from 'material-ui/svg-icons/editor/show-chart';
import CloudDownload from 'material-ui/svg-icons/file/cloud-download';
import * as React from 'react';
import {colors} from '../../../app/themes';
import {ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {Row, RowMiddle, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {IconProps, PopoverMenu} from '../../../components/popover/PopoverMenu';
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

const ToolbarIconButton = ({children, isSelected, onClick, tooltip}: IconButtonProps & Selectable) => (
  <IconButton
    onClick={onClick}
    className={classNames('ToolbarIconButton', {isSelected})}
    tooltip={tooltip}
    tooltipPosition="bottom-center"
    style={roundedIconStyle}
  >
    {children}
  </IconButton>
);

const ToolbarActionButton = (props: FlatButtonProps) => {
  const labelStyle: React.CSSProperties = {
    fontWeight: 'bold',
    fontSize: 12,
    ...props.labelStyle,
  };

  return (
    <FlatButton
      className={classNames('ToolbarActionButton', props.disabled ? 'disabled' : '')}
      labelPosition="after"
      {...props}
      labelStyle={labelStyle}
    />
  );
};

const LegendActionButton = ({onClick, disabled}: Clickable & IconProps) => (
  <ToolbarActionButton
    disabled={disabled}
    onClick={onClick}
    icon={<ContentFilterList color={disabled ? colors.borderColor : colors.lightBlack}/>}
    label={firstUpperTranslated('filter')}
    labelStyle={{color: disabled ? colors.borderColor : colors.lightBlack}}
  />
);

const anchorOrigin: origin = {horizontal: 'left', vertical: 'top'};
const targetOrigin: origin = {horizontal: 'left', vertical: 'top'};

const renderPopoverContent = () => (<LegendContainer/>);

export const Toolbar = ({
  changeToolbarView,
  hasMeasurements,
  resolution,
  selectResolution,
  toggleLegend,
  exportToExcel,
  isFetching,
  isExportingToExcel,
  view
}: Props) => {
  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  const excelExport = () => exportToExcel();

  const legendIconProps: IconProps = {disabled: !hasMeasurements};

  return (
    <RowSpaceBetween className="Toolbar">
      <Row>
        <RowMiddle className="Toolbar-ViewSettings">
          <ToolbarIconButton
            onClick={selectGraph}
            tooltip={firstUpperTranslated('graph')}
            isSelected={view === ToolbarView.graph}
          >
            <EditorShowChart color={colors.lightBlack}/>
          </ToolbarIconButton>
          <ToolbarIconButton
            onClick={selectTable}
            tooltip={firstUpperTranslated('table')}
            isSelected={view === ToolbarView.table}
          >
            <EditorFormatListBulleted color={colors.lightBlack} hoverColor={colors.iconHover}/>
          </ToolbarIconButton>
        </RowMiddle>

        <RowMiddle>
          <ToolbarActionButton
            disabled={true}
            icon={<ContentSave color={colors.borderColor}/>}
            label={firstUpperTranslated('save report')}
          />
          <ToolbarActionButton
            disabled={isFetching || isExportingToExcel || !hasMeasurements}
            onClick={excelExport}
            icon={<CloudDownload color={colors.lightBlack}/>}
            label={firstUpperTranslated('export to excel')}
          />
        </RowMiddle>
      </Row>

      <RowRight className={classNames('Tabs-DropdownMenus')}>
        <ResolutionSelection resolution={resolution} selectResolution={selectResolution}/>
        <PopoverMenu
          popoverClassName="Popover-Legend"
          IconComponent={LegendActionButton}
          iconProps={legendIconProps}
          anchorOrigin={anchorOrigin}
          targetOrigin={targetOrigin}
          renderPopoverContent={renderPopoverContent}
        />
      </RowRight>
    </RowSpaceBetween>
  );
};
