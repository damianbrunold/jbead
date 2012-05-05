object Aboutbox: TAboutbox
  Left = 215
  Top = 64
  BorderStyle = bsDialog
  Caption = 'Über DB-BEAD'
  ClientHeight = 505
  ClientWidth = 474
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'MS Sans Serif'
  Font.Style = []
  OldCreateOrder = False
  OnShow = FormShow
  PixelsPerInch = 96
  TextHeight = 13
  object text: TRichEdit
    Left = 0
    Top = 0
    Width = 376
    Height = 332
    TabStop = False
    BorderStyle = bsNone
    Color = clBtnFace
    ReadOnly = True
    TabOrder = 1
    WantReturns = False
  end
  object bOK: TButton
    Left = 288
    Top = 296
    Width = 75
    Height = 25
    Cancel = True
    Caption = 'OK'
    Default = True
    ModalResult = 1
    TabOrder = 0
  end
end
