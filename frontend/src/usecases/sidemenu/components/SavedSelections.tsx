import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {routes} from '../../../app/routes';
import {listItemInnerDivStyle, listItemStyle, listItemStyleSelected, secondaryBgHover} from '../../../app/themes';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Row, RowMiddle, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Medium} from '../../../components/texts/Texts';
import {history} from '../../../index';
import {firstUpperTranslated} from '../../../services/translationService';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {Callback, IdNamed, OnClickWithId, uuid} from '../../../types/Types';
import {DispatchToProps, StateToProps} from '../containers/SavedSelectionsContainer';
import {CreateNewSelectionListItem} from './CreateNewSelectionListItem';
import {LoadingListItems} from './LoadingListItems';
import {SavedSelectionActionsDropdown} from './SavedSelectionActionsDropdown';
import './SavedSelections.scss';
import './UserSelectionActionDropdown.scss';

interface ConfirmDelete {
  confirmDelete: OnClickWithId;
}

type Props = DispatchToProps & StateToProps;

const ListItems = ({
  confirmDelete,
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
        className="SavedSelection-ListItem"
        style={id === selection.id && isMeterPage ? listItemStyleSelected : listItemStyle}
        innerDivStyle={listItemInnerDivStyle}
        hoverColor={secondaryBgHover}
        key={`saved-${id}`}
      >
        <RowSpaceBetween>
          <RowMiddle className="SavedSelection-Name flex-1" onClick={onSelect}>
            <Medium className="first-uppercase">{name}</Medium>
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

  const items = savedSelections.isFetching
    ? [<LoadingListItems key="loading-list-item"/>]
    : [initialSelectionId, ...savedSelections.result].map(renderListItem);
  return (
    <>
      {items}
      <CreateNewSelectionListItem resetSelection={resetSelection}/>
    </>
  );
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
