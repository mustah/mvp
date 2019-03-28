import {default as classNames} from 'classnames';
import Card from 'material-ui/Card/Card';
import Divider from 'material-ui/Divider';
import ActionDelete from 'material-ui/svg-icons/action/delete';
import ImageEdit from 'material-ui/svg-icons/image/edit';
import * as React from 'react';
import {actionMenuItemIconStyle, cardStyle, dividerStyle, svgIconProps} from '../../../../app/themes';
import {ActionMenuItem} from '../../../../components/actions-dropdown/ActionMenuItem';
import {ActionsDropdown} from '../../../../components/actions-dropdown/ActionsDropdown';
import {useConfirmDialog} from '../../../../components/dialog/confirmDialogHook';
import {ConfirmDialog} from '../../../../components/dialog/DeleteConfirmDialog';
import {RowMiddle} from '../../../../components/layouts/row/Row';
import {WidgetTitle} from '../../../../components/texts/Titles';
import {translate} from '../../../../services/translationService';
import {Callback, Children, ClassNamed, OnClick, RenderFunction} from '../../../../types/Types';
import './Widget.scss';

interface Props extends ClassNamed {
  children: Children;
  containerStyle?: React.CSSProperties;
}

const Widget = ({children, className, containerStyle}: Props) => (
  <Card
    className={classNames('Widget', className)}
    style={cardStyle}
    containerStyle={containerStyle}
  >
    {children}
  </Card>
);

interface WidgetWithTitleProps extends Props {
  title: string;
  configure: OnClick;
  deleteWidget: Callback;
}

const EditIcon = <ImageEdit {...svgIconProps} style={actionMenuItemIconStyle}/>;

export const WidgetWithTitle = ({title, children, className, configure, deleteWidget}: WidgetWithTitleProps) => {
  const {isOpen, openConfirm, closeConfirm, confirm} = useConfirmDialog(deleteWidget);

  const renderPopoverContent: RenderFunction<OnClick> = (onClick: OnClick) => {
    const onClickEdit = () => {
      onClick();
      configure();
    };

    const onClickDelete = (w) => {
      onClick();
      openConfirm(w);
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
    <Widget className={className}>
      <RowMiddle className="space-between grid-draggable">
        <WidgetTitle>{title}</WidgetTitle>
        <ActionsDropdown className={'grid-not-draggable'} renderPopoverContent={renderPopoverContent}/>
      </RowMiddle>
      {children}
      <ConfirmDialog isOpen={isOpen} close={closeConfirm} confirm={confirm}/>
    </Widget>
  );
};
