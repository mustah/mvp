import {default as classNames} from 'classnames';
import IconButton from 'material-ui/IconButton';
import EditorFormatListBulleted from 'material-ui/svg-icons/editor/format-list-bulleted';
import * as React from 'react';
import {colors} from '../../../app/themes';
import {ResolutionSelection} from '../../../components/dates/ResolutionSelection';
import {IconReport} from '../../../components/icons/IconReport';
import {RowMiddle, RowRight, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {firstUpperTranslated} from '../../../services/translationService';
import {ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {Selectable} from '../../../types/Types';
import {Props} from '../containers/ToolbarContainer';
import './Toolbar.scss';
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
    tooltipPosition="top-center"
    style={roundedIconStyle}
  >
    {children}
  </IconButton>
);

export const Toolbar = ({changeToolbarView, resolution, selectResolution, view}: Props) => {
  const selectGraph = () => changeToolbarView(ToolbarView.graph);
  const selectTable = () => changeToolbarView(ToolbarView.table);
  return (
    <RowSpaceBetween className="Toolbar">
      <RowMiddle>
        <ToolbarIconButton
          onClick={selectGraph}
          tooltip={firstUpperTranslated('graph')}
          isSelected={view === ToolbarView.graph}
        >
          <IconReport color={colors.lightBlack} style={{height: 20}}/>
        </ToolbarIconButton>
        <ToolbarIconButton
          onClick={selectTable}
          tooltip={firstUpperTranslated('table')}
          isSelected={view === ToolbarView.table}
        >
          <EditorFormatListBulleted color={colors.lightBlack} hoverColor={colors.iconHover}/>
        </ToolbarIconButton>
      </RowMiddle>
      <RowRight className={classNames('Tabs-DropdownMenus')}>
        <ResolutionSelection resolution={resolution} selectResolution={selectResolution}/>
      </RowRight>
    </RowSpaceBetween>
  );
};
