import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {history, routes} from '../../../app/routes';
import {listItemStyle, listItemStyleSelected} from '../../../app/themes';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {withContent} from '../../../components/hoc/withContent';
import {ThemeContext, withCssStyles} from '../../../components/hoc/withThemeProvider';
import {Row, RowMiddle, RowSpaceBetween} from '../../../components/layouts/row/Row';
import {Medium} from '../../../components/texts/Texts';
import {firstUpperTranslated} from '../../../services/translationService';
import {initialSelectionId} from '../../../state/user-selection/userSelectionModels';
import {Callback, IdNamed, OnClickWithId, uuid} from '../../../types/Types';
import {toAggregateLegendItem} from '../../report/helpers/legendHelper';
import {DispatchToProps, StateToProps} from '../containers/SavedSelectionsContainer';
import {CreateNewSelectionListItem} from './CreateNewSelectionListItem';
import {LoadingListItems} from './LoadingListItems';
import {ActionDropdownProps, SavedSelectionActionsDropdown} from './SavedSelectionActionsDropdown';
import './SavedSelections.scss';
import './UserSelectionActionDropdown.scss';

interface ConfirmDelete {
  confirmDelete: OnClickWithId;
}

type Props = DispatchToProps & StateToProps;

const SavedSelectionActionsDropdownWrapper = withContent<ActionDropdownProps>(SavedSelectionActionsDropdown);

const ListItems = ({
  addToReport,
  confirmDelete,
  cssStyles: {secondary},
  isMeterPage,
  fetchUserSelections,
  resetSelection,
  savedSelections,
  selectSelection,
  selectSavedSelection,
  selection
}: Props & ConfirmDelete & ThemeContext) => {
  React.useEffect(() => {
    fetchUserSelections();
  }, [savedSelections]);

  const allMetersSelectionListItem = {id: initialSelectionId, name: firstUpperTranslated('all')};

  const renderListItem = (savedSelectionId: uuid) => {
    const {id, name}: IdNamed = savedSelectionId === initialSelectionId
      ? allMetersSelectionListItem
      : savedSelections.entities[savedSelectionId];

    const onShowAverageInReport: Callback = () => {
      history.push(routes.report);
      addToReport(toAggregateLegendItem({id, name}));
      selectSavedSelection(id);
    };

    const onEditSelection: Callback = () => selectSavedSelection(id);
    const onSelect: Callback = () => selectSelection(id);

    const selectedStyle: React.CSSProperties = {
      ...listItemStyleSelected,
      backgroundColor: secondary.bgActive,
      color: secondary.fgActive
    };
    return (
      <ListItem
        className="SavedSelection-ListItem"
        style={id === selection.id && isMeterPage ? selectedStyle : listItemStyle}
        innerDivStyle={{padding: 0}}
        hoverColor={secondary.bgHover}
        key={`saved-${id}`}
      >
        <RowSpaceBetween>
          <RowMiddle className="SavedSelection-Name flex-1" onClick={onSelect}>
            <Medium className="first-uppercase" style={{color: secondary.fg}}>{name}</Medium>
          </RowMiddle>
          <Row className="UserSelectionActionDropdown">
            <SavedSelectionActionsDropdownWrapper
              id={id}
              hasContent={id !== initialSelectionId}
              confirmDelete={confirmDelete}
              onShowAverageInReport={onShowAverageInReport}
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

const ThemedListItems = withCssStyles(ListItems);

export const SavedSelections = (props: Props) => {
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(props.deleteUserSelection);
  return (
    <>
      <ThemedListItems {...props} confirmDelete={openConfirm}/>
      <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
    </>
  );
};
