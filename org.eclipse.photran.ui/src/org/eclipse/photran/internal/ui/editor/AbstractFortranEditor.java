/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.MarginPainter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.photran.cdtinterface.ui.editor.CDTBasedSourceViewerConfiguration;
import org.eclipse.photran.cdtinterface.ui.editor.CDTBasedTextEditor;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.ui.FortranUIPlugin;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;

/**
 * Base class for the fixed and free-form Fortran editors
 * 
 * @author Jeff Overbey
 */
public abstract class AbstractFortranEditor extends CDTBasedTextEditor implements ISelectionChangedListener
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String SOURCE_VIEWER_CONFIG_EXTENSION_POINT_ID =
        "org.eclipse.photran.ui.sourceViewerConfig";
    
    public static String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE };
    
    protected static String FORTRAN_EDITOR_CONTEXT_ID = "org.eclipse.photran.ui.FortranEditorContext";
    
    protected static String BLOCK_COMMENT_COMMAND_ID = "org.eclipse.photran.ui.CommentCommand";
    
    protected static final RGB VERTICAL_LINE_COLOR = new RGB(176, 180, 185);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Public Fields - Custom Reconciler Task Support
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /** @see org.eclipse.photran.internal.ui.editor_vpg.FortranVPGReconcilingStrategy */
    public Object reconcilerTasks = null;

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    protected IPreferenceStore fCombinedPreferenceStore;
    protected Composite fMainComposite;
    protected FortranHorizontalRuler fHRuler;
    protected Color verticalLineColor;
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public AbstractFortranEditor()
    {
        super();
        setSourceViewerConfiguration(createSourceViewerConfiguration());
        setRangeIndicator(new DefaultRangeIndicator());
        useCDTDocumentProvider();
        
        // This has to be set to be notified of changes to preferences
        // Without this, the editor will not auto-update
        IPreferenceStore store = FortranUIPlugin.getDefault().getPreferenceStore();
        IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
        fCombinedPreferenceStore = new ChainedPreferenceStore(new IPreferenceStore[] { store, generalTextStore, getPreferenceStore()});
        setPreferenceStore(fCombinedPreferenceStore);
        // This enables any global changes to editor e.g. font type and size to take effect
        WorkbenchChainedTextFontFieldEditor.startPropagate(store, JFaceResources.TEXT_FONT);

        useCDTRulerContextMenuID();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // JFace Text Overrides
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void doSetInput(IEditorInput input) throws CoreException
    {
        super.doSetInput(input);
        IDocument document = this.getDocumentProvider().getDocument(input);
        if (document == null) return;
        
        configurePartitionScanner(document);
    }

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);

        Composite childComp = (Composite)((Composite) parent.getChildren()[0]).getChildren()[0];
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 2;
        childComp.setLayout(layout);

        GridData data = new GridData(GridData.FILL_BOTH);
        childComp.getChildren()[0].setLayoutData(data);

        fMainComposite = childComp;

        createHorizontalRuler(fMainComposite);
        createLightGrayLines();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Ctrl+/ Block Commenting Support
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    protected void initializeKeyBindingScopes()
    {
        setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope", FORTRAN_EDITOR_CONTEXT_ID });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Custom Ruler
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param mainComposite
     * This creates the horizontal ruler and adds it to the top of the editor
     */
    protected void createHorizontalRuler(Composite mainComposite)
    {
        GC gc = new GC(getSourceViewer().getTextWidget());
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = gc.getFontMetrics().getHeight();
        gc.dispose();

        fHRuler = getFortranHorizontalRuler(mainComposite);
        fHRuler.setFont(getSourceViewer().getTextWidget().getFont());
        fHRuler.setSourceViewer(getSourceViewer());
        fHRuler.setLayoutData(data);
        fHRuler.moveAbove(null);
    }

    protected abstract FortranHorizontalRuler getFortranHorizontalRuler(Composite mainComposite);

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Gray Vertical Lines
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Display a light gray line between columns 6/7 and 72/73
     */
    protected void createLightGrayLines()
    {
        verticalLineColor = new Color(null, VERTICAL_LINE_COLOR);
        
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof ITextViewerExtension2)
        {
            ITextViewerExtension2 painter = (ITextViewerExtension2)sourceViewer;
            
            int[] columns = getColumnsToDrawVerticalLinesOn();
            for (int i = 0; i < columns.length; i++)
            {
                MarginPainter p = new MarginPainter(getSourceViewer());
                p.setMarginRulerColumn(columns[i]);
                p.setMarginRulerColor(verticalLineColor);
                painter.addPainter(p);
            }
        }
    }

    protected abstract int[] getColumnsToDrawVerticalLinesOn();

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Preference Page Support
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns true if the event will require us to perform a damage and repair
     * e.g. a color preference change
     */
    protected boolean affectsTextPresentation(PropertyChangeEvent event)
    {
        return FortranPreferences.respondToPreferenceChange(event.getProperty())
               || super.affectsTextPresentation(event);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Extensible SourceViewerConfiguration (permits Declaration View, content assist, etc.)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        // If org.eclipse.photran.vpg.ui is contributing a SourceViewerConfiguration through the
        // extension point, load it
        IConfigurationElement[] configs = Platform.getExtensionRegistry().
            getConfigurationElementsFor(SOURCE_VIEWER_CONFIG_EXTENSION_POINT_ID);
        if (configs.length > 0)
        {
            try
            {
                IFortranSourceViewerConfigurationFactory factory =
                    (IFortranSourceViewerConfigurationFactory)
                    configs[configs.length-1].createExecutableExtension("factory");
                return factory.create(AbstractFortranEditor.this);
            }
            catch (CoreException e)
            {
                // Fall through
            }
        }

        // Otherwise, default to CDT's reconciler
        return new FortranSourceViewerConfiguration(this);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Syntax Highlighting and Outline View Support (Partitioning and Reconciling)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void configurePartitionScanner(IDocument document)
    {
        IDocumentPartitioner partitioner = new FastPartitioner(new RuleBasedPartitionScanner(),
                                                               PARTITION_TYPES);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);
    }

    protected abstract ITokenScanner getTokenScanner();

    public static class FortranSourceViewerConfiguration extends CDTBasedSourceViewerConfiguration
    {
        protected PresentationReconciler reconciler;
        
        public FortranSourceViewerConfiguration(AbstractFortranEditor editor)
        {
            super(editor);
        }
        
        /**
         * Returns a list of the possible partitions' content types.
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
         */
        public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
        {
            return PARTITION_TYPES;
        }

        /**
         * Sets up rules for syntax highlighting.
         * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
         */
        public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
        {
            if (reconciler == null)
            {
                reconciler = new PresentationReconciler();
        
                // Set up a damager-repairer for each content type
                
                DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTokenScanner());
                reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
                reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
            }
   
            return reconciler;
        }

        protected ITokenScanner getTokenScanner()
        {
            return ((AbstractFortranEditor)editor).getTokenScanner();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public abstract boolean isFixedForm();
    
    public IFile getIFile()
    {
        IEditorInput input = getEditorInput();
        return (input != null && input instanceof IFileEditorInput ? ((IFileEditorInput)input).getFile() : null);
    }

    public IDocument getIDocument()
    {
        IEditorInput input = getEditorInput();
        if (input == null) return null;
        
        IDocumentProvider dp = getDocumentProvider();
        if (dp == null) return null;
        
        return dp.getDocument(input);
    }
    
    public ITextSelection getSelection()
    {
        ISelectionProvider provider = getSelectionProvider();
        if (provider == null) return null;
        
        ISelection sel = provider.getSelection();
        if (!(sel instanceof ITextSelection)) return null;
        
        return (ITextSelection)sel;
    }
    
    public Shell getShell()
    {
        return getSite().getShell();
    }

    public ISourceViewer getSourceViewerx() // Annoyingly, the superclass method is declared final
    {
        return super.getSourceViewer();
    }
    
    public IReconciler getReconciler()
    {
        return getSourceViewerConfiguration().getReconciler(getSourceViewer());
    }

    public void forceOutlineViewUpdate()
    {
        //  //     ///     ////   //  //    ///
        //  //   //  //   //      // //    /////
        //////   //////   //      ////      ///
        //  //   //  //   //      // //
        //  //   //  //    ////   //  //    //
      
        IDocument doc = getIDocument();
        if (doc == null) return;
        doc.set(" " + doc.get());
        doSave(null);
        doc.set(doc.get().substring(1));
        doSave(null);
    }
}
