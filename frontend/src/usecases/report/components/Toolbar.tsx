import {default as classNames} from 'classnames';
import FlatButton from 'material-ui/FlatButton';
import IconButton from 'material-ui/IconButton';
import ContentFilterList from 'material-ui/svg-icons/content/filter-list';
import ContentSave from 'material-ui/svg-icons/content/save';
import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import * as React from 'react';
import {colors} from '../../../app/themes';
import {ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {IconReport} from '../../../components/icons/IconReport';
import {Row, RowMiddle, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {firstUpperTranslated} from '../../../services/translationService';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {Selectable} from '../../../types/Types';
import {Props} from '../containers/ToolbarContainer';
import './Toolbar.scss';
import FlatButtonProps = __MaterialUI.FlatButtonProps;
import IconButtonProps = __MaterialUI.IconButtonProps;

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

const ToolbarActionButton = (props: FlatButtonProps) => (
  <FlatButton
    labelPosition="after"
    {...props}
    labelStyle={{fontWeight: 'bold', color: colors.lightBlack, fontSize: 12, ...props.labelStyle}}
  />
);

export const Toolbar = ({changeToolbarView, resolution, selectResolution, toggleLegend, view}: Props) => {
  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  return (
    <RowSpaceBetween className="Toolbar">
      <Row>
        <RowMiddle className="Toolbar-ViewSettings">
          <ToolbarIconButton
            onClick={selectGraph}
            tooltip={firstUpperTranslated('graph')}
            isSelected={view === ToolbarView.graph}
          >
            <IconReport color={colors.lightBlack}/>
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
            icon={<ContentFilterList color={colors.lightBlack}/>}
            label={firstUpperTranslated('legend')}
            onClick={toggleLegend}
          />
          <ToolbarActionButton
            disabled={true}
            icon={<ContentSave color={colors.borderColor}/>}
            label={firstUpperTranslated('save')}
            labelStyle={{color: colors.borderColor}}
          />
        </RowMiddle>
      </Row>

      <RowRight className={classNames('Tabs-DropdownMenus')}>
        <ResolutionSelection resolution={resolution} selectResolution={selectResolution}/>
      </RowRight>
    </RowSpaceBetween>
  );
};
