import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {listItemStyle, listItemStyleSelected, secondaryBgHover} from '../../../app/themes';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Row, RowMiddle, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Normal} from '../../../components/texts/Texts';
import {history} from '../../../index';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {Callback, IdNamed, OnClickWithId, uuid} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/SavedSelectionsContainer';
import {LoadingTreeViewItems} from './LoadingTreeViewItems';
import {SavedSelectionActionsDropdown} from './SavedSelectionActionsDropdown';
import './SavedSelections.scss';
import './UserSelectionActionDropdown.scss';

const innerDivStyle: React.CSSProperties = {
  padding: 0,
};

interface ConfirmDelete {
  confirmDelete: OnClickWithId;
}

type Props = DispatchToProps & StateToProps;

const ListItems = ({
  confirmDelete,
  isFetching,
  isMeterPage,
  fetchUserSelections,
  resetSelection,
  savedSelections,
  selectSavedSelection,
  selection
}: Props & ConfirmDelete) => {
  React.useEffect(() => {
    fetchUserSelections();
  }, [savedSelections]);

  const allMetersSelectionListItem = {id: initialSelectionId, name: firstUpperTranslated('all')};

  const renderListItem = (savedSelectionId: uuid) => {
    const {id, name}: IdNamed = savedSelectionId === initialSelectionId
      ? allMetersSelectionListItem
      : savedSelections.entities[savedSelectionId];

    const onAddAllToReport: Callback = () => {
      history.push(routes.report);
      selectSavedSelection(id);
    };
    const onEditSelection: Callback = () => selectSavedSelection(id);
    const onSelect: Callback = () => {
      history.push(routes.meter);
      if (id === initialSelectionId) {
        resetSelection();
      } else {
        selectSavedSelection(id);
      }
    };

    return (
      <ListItem
        className="UserSelection-ListItem"
        style={id === selection.id && isMeterPage ? listItemStyleSelected : listItemStyle}
        innerDivStyle={innerDivStyle}
        hoverColor={secondaryBgHover}
        key={`saved-${id}`}
      >
        <RowSpaceBetween>
          <RowMiddle className="UserSelectionName flex-1" onClick={onSelect}>
            <Normal>{name}</Normal>
          </RowMiddle>
          <Row className="UserSelectionActionDropdown">
            <SavedSelectionActionsDropdown
              id={id}
              confirmDelete={confirmDelete}
              onAddAllToReport={onAddAllToReport}
              onEditSelection={onEditSelection}
            />
          </Row>
        </RowSpaceBetween>
      </ListItem>
    );
  };

  const savedSelectionIds = savedSelections.result;

  const items = savedSelectionIds.length
    ? [initialSelectionId, ...savedSelectionIds].map(renderListItem)
    : [
      (
        <LoadingTreeViewItems
          isFetching={isFetching}
          text={translate('no saved selections')}
          key="loading-list-item"
        />
      )
    ];
  return <>{items}</>;
};

export const SavedSelections = (props: Props) => {
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(props.deleteUserSelection);
  return (
    <>
      <ListItems {...props} confirmDelete={openConfirm}/>
      <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
    </>
  );
};
