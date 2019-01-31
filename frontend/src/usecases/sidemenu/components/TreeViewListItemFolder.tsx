import * as React from 'react';
import {routes} from '../../../app/routes';
import {actionMenuItemIconStyle, menuItemStyle} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {IconReport} from '../../../components/icons/IconReport';
import {Row, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {FirstUpper} from '../../../components/texts/Texts';
import {history} from '../../../index';
import {translate} from '../../../services/translationService';
import {Identifiable, OnClick, OnClickWithId, RenderFunction} from '../../../types/Types';
import {TreeViewListItemProps} from '../containers/TreeViewListItemGroupContainer';
import './UserSelectionActionDropdown.scss';

const treeViewItemStyle: React.CSSProperties = {
  paddingLeft: 8,
  paddingTop: 6,
  paddingBottom: 6,
  cursor: 'pointer',
  ...menuItemStyle.textStyle,
};

interface Props extends Identifiable {
  addToReport: OnClickWithId;
}

const TreeViewFolderActionDropdown = ({id, addToReport}: Props) => {

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const addAverageToReport = () => {
      onClick();
      history.push(routes.report);
      addToReport(id);
    };

    return [
      (
        <ActionMenuItem
          leftIcon={<IconReport style={actionMenuItemIconStyle}/>}
          name={translate('show average in report')}
          onClick={addAverageToReport}
          key={`show-average-in-report-${id}`}
        />
      ),
    ];
  };

  return <ActionsDropdown renderPopoverContent={renderPopoverContent}/>;
};

export const TreeViewListItemFolder = ({addToReport, id, text}: TreeViewListItemProps) => (
  <RowSpaceBetween className="flex-1 flex-nowrap">
    <FirstUpper style={treeViewItemStyle}>{text}</FirstUpper>
    <Row className="UserSelectionActionDropdown">
      <TreeViewFolderActionDropdown id={id} addToReport={addToReport}/>
    </Row>
  </RowSpaceBetween>);
