import {default as classNames} from 'classnames';
import Card from 'material-ui/Card/Card';
import Divider from 'material-ui/Divider/index';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {actionMenuItemIconStyle, cardStyle, dividerStyle, svgIconProps} from '../../../app/themes';
import {ActionMenuItem} from '../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../components/actions-dropdown/ActionsDropdown';
import {useConfirmDialog} from '../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../components/dialog/DeleteConfirmDialog';
import {Column} from '../../../components/layouts/column/Column';
import {RowMiddle} from '../../../components/layouts/row/Row';
import {WidgetTitle} from '../../../components/texts/Titles';
import {translate} from '../../../services/translationService';
import {OnClick, RenderFunction, uuid, WithChildren} from '../../../types/Types';
import {WidgetDispatchProps} from '../dashboardModels';
import './Widget.scss';

interface WidgetWithTitleProps extends WidgetDispatchProps, WithChildren {
  containerStyle?: React.CSSProperties;
  headerClassName?: string;
  title: string;
}

const EditIcon = <ImageEdit {...svgIconProps} style={actionMenuItemIconStyle}/>;

export const WidgetWithTitle = ({
  children,
  containerStyle,
  editWidget,
  deleteWidget,
  headerClassName,
  title,
}: WidgetWithTitleProps) => {
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteWidget);

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickEdit = () => {
      onClick();
      editWidget();
    };

    const onClickDelete = (widgetId: uuid) => {
      onClick();
      openConfirm(widgetId);
    };

    return [
      (
        <ActionMenuItem
          key="edit-widget"
          leftIcon={EditIcon}
          name={translate('edit widget')}
          onClick={onClickEdit}
        />
      ),
      <Divider key="divider" style={dividerStyle}/>,
      (
        <ActionMenuItem
          key="delete-widget"
          leftIcon={<ActionDelete style={actionMenuItemIconStyle}/>}
          name={translate('delete widget')}
          onClick={onClickDelete}
        />
      )
    ];
  };

  return (
    <Card className="Widget" containerStyle={containerStyle} style={cardStyle}>
      <Column className={classNames('Widget-LeftBorder', headerClassName)}>
        <RowMiddle className="Widget-Title space-between grid-draggable">
          <WidgetTitle>{title}</WidgetTitle>
          <ActionsDropdown className={'grid-not-draggable'} renderPopoverContent={renderPopoverContent}/>
        </RowMiddle>
      </Column>
      {children}
      <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
    </Card>
  );
};
